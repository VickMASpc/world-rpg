package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourcePool;
import dev.worldrpg.combat.stat.StatKey;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Shared production resolution topology for runtime and headless simulation.
 *
 * <p>Calibration constants are supplied by CombatMathProfile. This class owns
 * ordering and equations, not a canonical balance profile.</p>
 */
public final class ProfiledCombatResolver
        implements CombatResolutionGateway {
    private final CombatMathProfile profile;
    private final CombatRollSource rolls;
    private final CombatStatResolver stats;

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls
    ) {
        this(
                profile,
                rolls,
                CombatStatResolver.direct()
        );
    }

    public ProfiledCombatResolver(
            CombatMathProfile profile,
            CombatRollSource rolls,
            CombatStatResolver stats
    ) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.rolls = Objects.requireNonNull(rolls, "rolls");
        this.stats = Objects.requireNonNull(stats, "stats");
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

        ResourcePool health = target.resources()
                .require(profile.healthResource());

        double power = stats.value(source, 
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
                1.0 + stats.value(source, 
                        outgoingBonusStat(request.kind())
                )
        );
        double afterOutgoing =
                afterScaling * outgoingMultiplier;

        double criticalChance = clamp(
                stats.value(source, 
                        CombatMathStats.CRIT_CHANCE
                ),
                0.0,
                profile.maximumCriticalChance()
        );

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
                        ? mitigation(target, school)
                        : 0.0;
        double afterMitigation =
                afterCritical * (1.0 - mitigationFraction);

        double incomingMultiplier = Math.max(
                0.0,
                1.0 + stats.value(target, 
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
                new CombatMagnitudeResolvedEvent(
                        request.gameTick(),
                        request.kind(),
                        source.id(),
                        target.id(),
                        request.causeId(),
                        request.schoolId(),
                        trace
                );

        ResourceChangedEvent resourceChanged =
                new ResourceChangedEvent(
                        request.gameTick(),
                        target.id(),
                        profile.healthResource(),
                        change
                );

        return List.of(resolved, resourceChanged);
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
            CombatActor target,
            CombatSchoolKey school
    ) {
        double defense = Math.max(
                0.0,
                stats.value(target, 
                        CombatMathStats.resistanceFor(school)
                )
        );

        if (defense == 0.0) {
            return 0.0;
        }

        double raw = defense
                / (defense + profile.mitigationScale());

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
