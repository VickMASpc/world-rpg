package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatActorDefeatedEvent;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourcePool;
import dev.worldrpg.combat.stat.StatKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Shared production resolution topology for runtime and headless simulation.
 *
 * <p>Calibration constants are supplied by P4 profiles. This class owns
 * ordering and equations, not canonical balance values.</p>
 */
public final class ProfiledCombatResolver
        implements CombatResolutionGateway {
    private static final dev.worldrpg.api.id.RpgId UNKNOWN_PROFILE =
            dev.worldrpg.api.id.RpgId.parse(
                    "world_rpg:condition/unknown_resolution_profile"
            );
    private static final dev.worldrpg.api.id.RpgId MISSING_HEALTH =
            dev.worldrpg.api.id.RpgId.parse(
                    "world_rpg:condition/missing_health_resource"
            );
    private static final dev.worldrpg.api.id.RpgId UNSUPPORTED_SCHOOL =
            dev.worldrpg.api.id.RpgId.parse(
                    "world_rpg:condition/unsupported_combat_school"
            );
    private static final dev.worldrpg.api.id.RpgId INVALID_LEVEL =
            dev.worldrpg.api.id.RpgId.parse(
                    "world_rpg:condition/invalid_combat_level"
            );
    private final CombatMathProfile profile;
    private final CombatRollSource rolls;
    private final CombatStatResolver stats;
    private final CombatLevelSource levels;
    private final CombatOutcomeProfileSet outcomes;

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls
    ) {
        this(
                profile,
                rolls,
                CombatStatResolver.direct(),
                CombatLevelSource.constant(1),
                CombatOutcomeProfileSet.guaranteedOnly()
        );
    }

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls,
            CombatStatResolver stats
    ) {
        this(
                profile,
                rolls,
                stats,
                CombatLevelSource.constant(1),
                CombatOutcomeProfileSet.guaranteedOnly()
        );
    }

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls,
            CombatStatResolver stats,
            CombatLevelSource levels
    ) {
        this(
                profile,
                rolls,
                stats,
                levels,
                CombatOutcomeProfileSet.guaranteedOnly()
        );
    }

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls,
            CombatStatResolver stats,
            CombatLevelSource levels,
            CombatOutcomeProfileSet outcomes
    ) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.rolls = Objects.requireNonNull(rolls, "rolls");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.levels = Objects.requireNonNull(levels, "levels");
        this.outcomes = Objects.requireNonNull(outcomes, "outcomes");
    }

    @Override
    public ConditionResult validate(
            CombatMagnitudeRequest request
    ) {
        Objects.requireNonNull(request, "request");

        ConditionResult result = ConditionResult.pass();

        if (outcomes.find(request.resolutionProfileId()).isEmpty()) {
            result = result.plus(
                    ConditionResult.fail(
                            UNKNOWN_PROFILE,
                            "Unknown combat resolution profile: "
                                    + request.resolutionProfileId()
                    )
            );
        }

        if (request.target()
                .resources()
                .find(profile.healthResource())
                .isEmpty()) {
            result = result.plus(
                    ConditionResult.fail(
                            MISSING_HEALTH,
                            "Target lacks health resource "
                                    + profile.healthResource()
                    )
            );
        }

        if (request.kind() == CombatMagnitudeKind.DAMAGE) {
            try {
                CombatMathStats.resistanceFor(
                        new CombatSchoolKey(request.schoolId())
                );
            } catch (IllegalArgumentException exception) {
                result = result.plus(
                        ConditionResult.fail(
                                UNSUPPORTED_SCHOOL,
                                exception.getMessage()
                        )
                );
            }

            try {
                profile.mitigationScale().valueAt(
                        levels.levelOf(request.source())
                );
            } catch (RuntimeException exception) {
                result = result.plus(
                        ConditionResult.fail(
                                INVALID_LEVEL,
                                exception.getMessage() == null
                                        ? "Invalid combat level"
                                        : exception.getMessage()
                        )
                );
            }
        }

        return result;
    }

    @Override
    public List<CombatEvent> resolve(
            CombatMagnitudeRequest request
    ) {
        Objects.requireNonNull(request, "request");

        CombatActor source = request.source();
        CombatActor target = request.target();
        CombatSchoolKey school =
                new CombatSchoolKey(request.schoolId());

        CombatOutcomeProfile outcomeProfile =
                outcomes.require(request.resolutionProfileId());

        ResourcePool health = target.resources()
                .require(profile.healthResource());

        double missChance = clamp(
                outcomeProfile.baseMissChance()
                        - stats.value(
                                source,
                                CombatMathStats.HIT_CHANCE
                        ),
                0.0,
                1.0
        );

        OptionalDouble contactRoll = OptionalDouble.empty();
        CombatContactOutcome contactOutcome =
                CombatContactOutcome.HIT;

        if (missChance > 0.0) {
            double roll = rolls.nextUnit();
            requireUnitRoll(roll);
            contactRoll = OptionalDouble.of(roll);

            if (roll < missChance) {
                contactOutcome = CombatContactOutcome.MISS;
            }
        }

        if (contactOutcome == CombatContactOutcome.MISS) {
            CombatResolutionTrace trace =
                    missedTrace(
                            request,
                            missChance,
                            contactRoll
                    );

            return List.of(
                    resolvedEvent(request, trace)
            );
        }

        double power = stats.value(
                source,
                powerStat(request.kind(), school)
        );
        double coefficient = powerCoefficient(
                request.kind(),
                school
        );
        double powerContribution = power * coefficient;

        double afterScaling = Math.max(
                0.0,
                request.authoredBaseMagnitude()
                        + powerContribution
        );

        double outgoingMultiplier = Math.max(
                0.0,
                1.0 + stats.value(
                        source,
                        outgoingBonusStat(request.kind())
                )
        );
        double afterOutgoing =
                afterScaling * outgoingMultiplier;

        double criticalChance =
                outcomeProfile.canCrit()
                        ? clamp(
                                stats.value(
                                        source,
                                        CombatMathStats.CRIT_CHANCE
                                ),
                                0.0,
                                profile.maximumCriticalChance()
                        )
                        : 0.0;

        OptionalDouble criticalRoll = OptionalDouble.empty();
        boolean critical = false;

        if (afterOutgoing > 0.0 && criticalChance > 0.0) {
            double roll = rolls.nextUnit();
            requireUnitRoll(roll);
            criticalRoll = OptionalDouble.of(roll);
            critical = roll < criticalChance;
        }

        double criticalMultiplier = critical
                ? profile.criticalMultiplier()
                : 1.0;
        double afterCritical =
                afterOutgoing * criticalMultiplier;

        double mitigationFraction =
                request.kind() == CombatMagnitudeKind.DAMAGE
                        ? mitigation(source, target, school)
                        : 0.0;
        double afterMitigation =
                afterCritical * (1.0 - mitigationFraction);

        double incomingMultiplier = Math.max(
                0.0,
                1.0 + stats.value(
                        target,
                        incomingBonusStat(request.kind())
                )
        );

        double requestedFinal =
                afterMitigation * incomingMultiplier;

        ResourceChange change =
                request.kind() == CombatMagnitudeKind.DAMAGE
                        ? health.drainUpTo(requestedFinal)
                        : health.gain(requestedFinal);

        double excess =
                Math.max(0.0, requestedFinal - change.applied());

        CombatResolutionTrace trace =
                new CombatResolutionTrace(
                        request.resolutionProfileId(),
                        contactOutcome,
                        missChance,
                        contactRoll,
                        request.authoredBaseMagnitude(),
                        powerContribution,
                        afterScaling,
                        outgoingMultiplier,
                        afterOutgoing,
                        criticalChance,
                        criticalRoll,
                        critical,
                        criticalMultiplier,
                        afterCritical,
                        mitigationFraction,
                        afterMitigation,
                        incomingMultiplier,
                        requestedFinal,
                        change.applied(),
                        excess
                );

        CombatMagnitudeResolvedEvent resolved =
                resolvedEvent(request, trace);

        ResourceChangedEvent resourceChanged =
                new ResourceChangedEvent(
                        request.gameTick(),
                        target.id(),
                        profile.healthResource(),
                        change
                );

        List<CombatEvent> events =
                new ArrayList<>();
        events.add(resolved);
        events.add(resourceChanged);

        if (request.kind() == CombatMagnitudeKind.DAMAGE
                && change.before() > 0.0
                && change.after() == 0.0) {
            events.add(
                    new CombatActorDefeatedEvent(
                            request.gameTick(),
                            source.id(),
                            target.id(),
                            request.causeId()
                    )
            );
        }

        return List.copyOf(events);
    }

    private CombatResolutionTrace missedTrace(
            CombatMagnitudeRequest request,
            double missChance,
            OptionalDouble contactRoll
    ) {
        return new CombatResolutionTrace(
                request.resolutionProfileId(),
                CombatContactOutcome.MISS,
                missChance,
                contactRoll,
                request.authoredBaseMagnitude(),
                0.0,
                0.0,
                1.0,
                0.0,
                0.0,
                OptionalDouble.empty(),
                false,
                1.0,
                0.0,
                0.0,
                0.0,
                1.0,
                0.0,
                0.0,
                0.0
        );
    }

    private CombatMagnitudeResolvedEvent resolvedEvent(
            CombatMagnitudeRequest request,
            CombatResolutionTrace trace
    ) {
        return new CombatMagnitudeResolvedEvent(
                request.gameTick(),
                request.kind(),
                request.source().id(),
                request.target().id(),
                request.causeId(),
                request.schoolId(),
                trace
        );
    }

    private StatKey powerStat(
            CombatMagnitudeKind kind,
            CombatSchoolKey school
    ) {
        if (kind == CombatMagnitudeKind.HEALING) {
            return CombatMathStats.HEALING_POWER;
        }

        return school.equals(CombatSchools.PHYSICAL)
                ? CombatMathStats.ATTACK_POWER
                : CombatMathStats.SPELL_POWER;
    }

    private double powerCoefficient(
            CombatMagnitudeKind kind,
            CombatSchoolKey school
    ) {
        if (kind == CombatMagnitudeKind.HEALING) {
            return profile.healingPowerCoefficient();
        }

        return school.equals(CombatSchools.PHYSICAL)
                ? profile.physicalPowerCoefficient()
                : profile.spellPowerCoefficient();
    }

    private StatKey outgoingBonusStat(
            CombatMagnitudeKind kind
    ) {
        return kind == CombatMagnitudeKind.DAMAGE
                ? CombatMathStats.DAMAGE_DONE_BONUS
                : CombatMathStats.HEALING_DONE_BONUS;
    }

    private StatKey incomingBonusStat(
            CombatMagnitudeKind kind
    ) {
        return kind == CombatMagnitudeKind.DAMAGE
                ? CombatMathStats.DAMAGE_TAKEN_BONUS
                : CombatMathStats.HEALING_TAKEN_BONUS;
    }

    private double mitigation(
            CombatActor source,
            CombatActor target,
            CombatSchoolKey school
    ) {
        double defense = Math.max(
                0.0,
                stats.value(
                        target,
                        CombatMathStats.resistanceFor(school)
                )
        );

        if (defense == 0.0) {
            return 0.0;
        }

        double scale = profile.mitigationScale()
                .valueAt(levels.levelOf(source));

        double raw = defense
                / (defense + scale);

        return clamp(
                raw,
                0.0,
                profile.maximumMitigation()
        );
    }

    private static void requireUnitRoll(double roll) {
        if (!Double.isFinite(roll)
                || roll < 0.0
                || roll >= 1.0) {
            throw new IllegalStateException(
                    "CombatRollSource produced value outside [0, 1): "
                            + roll
            );
        }
    }

    private static double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }
}
