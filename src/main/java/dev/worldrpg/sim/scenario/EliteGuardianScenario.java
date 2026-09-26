package dev.worldrpg.sim.scenario;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.LookupCombatCastControlGateway;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.effect.CombatMagnitudeEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.InterruptEffect;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.math.CombatLevelSource;
import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.CombatMathStats;
import dev.worldrpg.combat.math.CombatSchools;
import dev.worldrpg.combat.math.CombatStatResolver;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.math.SeededCombatRollSource;
import dev.worldrpg.combat.math.WorldRpgDefenseDraft;
import dev.worldrpg.combat.math.WorldRpgOutcomeDraft;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatPowerScaling;
import dev.worldrpg.combat.resolution.CombatPowerTerm;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.target.TargetConditions;
import dev.worldrpg.combat.target.TargetObservation;
import dev.worldrpg.progression.WorldRpgMagnitudeDraft;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.CombatSimulationScenario;
import dev.worldrpg.sim.HeadlessAbilityDriver;
import dev.worldrpg.sim.HeadlessCombatSimulator;
import dev.worldrpg.sim.PriorityAbilityPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * First long-form elite calibration.
 *
 * <p>The player begins with a mana-efficient priority rotation and must fall
 * back to a zero-cost attack after exhausting mana while also interrupting a
 * dangerous long cast on cooldown.</p>
 */
public final class EliteGuardianScenario
        implements CombatSimulationScenario {
    public static final int LEVEL = 20;
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    public static final CombatActorId PLAYER_ID =
            new CombatActorId(1);
    public static final CombatActorId ELITE_ID =
            new CombatActorId(2);

    public static final RpgId BOLT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/elite_bolt"
            );
    public static final RpgId FALLBACK_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/elite_fallback"
            );
    public static final RpgId INTERRUPT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/elite_interrupt"
            );
    public static final RpgId BASIC_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/elite_basic"
            );
    public static final RpgId HEAVY_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/elite_heavy"
            );

    private static final long STEP_TICKS = 10L;
    private static final long MAX_FIGHT_TICKS = 7_200L;
    private static final long INTERRUPT_REACTION_TICKS = 20L;

    @Override
    public CombatSimulationReport run(long seed) {
        return runDetailed(seed).report();
    }

    public DetailedResult runDetailed(long seed) {
        CombatActor player =
                new CombatActor(PLAYER_ID);
        CombatActor elite =
                new CombatActor(ELITE_ID);

        double referenceHealth =
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .valueAt(LEVEL);
        double referenceResource =
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE
                        .valueAt(LEVEL);
        double referencePower =
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .valueAt(LEVEL);

        player.resources().add(
                HEALTH,
                referenceHealth,
                referenceHealth
        );
        player.resources().add(
                MANA,
                referenceResource,
                referenceResource
        );
        elite.resources().add(
                HEALTH,
                referenceHealth * 4.0,
                referenceHealth * 4.0
        );

        player.stats().setBase(
                CombatMathStats.SPELL_POWER,
                referencePower
        );
        player.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.05
        );
        player.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(LEVEL)
        );

        elite.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower
        );
        elite.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.03
        );
        elite.stats().setBase(
                CombatMathStats.ARCANE_RESISTANCE,
                WorldRpgDefenseDraft.referenceResistance(LEVEL)
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        new CombatMathProfile(
                                HEALTH,
                                0.35,
                                0.50,
                                0.50,
                                2.0,
                                0.50,
                                WorldRpgDefenseDraft.MITIGATION_SCALE,
                                0.75
                        ),
                        new SeededCombatRollSource(seed),
                        CombatStatResolver.direct(),
                        CombatLevelSource.constant(LEVEL),
                        WorldRpgOutcomeDraft.referenceProfiles()
                );

        HeadlessCombatSimulator simulator =
                new HeadlessCombatSimulator(resolver);

        AbilityObservationProvider observations =
                aliveObservationProvider();

        HeadlessAbilityDriver playerDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        player,
                        new CooldownBook(),
                        observations
                );
        HeadlessAbilityDriver eliteDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        elite,
                        new CooldownBook(),
                        observations
                );

        Map<CombatActorId, dev.worldrpg.combat.cast.CastController>
                controllers = Map.of(
                        player.id(),
                        playerDriver.casts(),
                        elite.id(),
                        eliteDriver.casts()
                );

        LookupCombatCastControlGateway castControl =
                new LookupCombatCastControlGateway(
                        id -> Optional.ofNullable(
                                controllers.get(id)
                        )
                );

        AbilityDefinition interrupt =
                interruptAbility(castControl);
        AbilityDefinition bolt =
                damageAbility(
                        BOLT_ID,
                        50,
                        0,
                        4.0,
                        10.0,
                        CombatSchools.ARCANE.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.SPELL_POWER,
                                        0.50
                                )
                        ),
                        resolver
                );
        AbilityDefinition fallback =
                damageAbility(
                        FALLBACK_ID,
                        80,
                        0,
                        0.0,
                        6.0,
                        CombatSchools.ARCANE.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.SPELL_POWER,
                                        0.30
                                )
                        ),
                        resolver
                );
        AbilityDefinition heavy =
                damageAbility(
                        HEAVY_ID,
                        80,
                        600,
                        0.0,
                        25.0,
                        CombatSchools.SHADOW.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        CombatPowerScaling.none(),
                        resolver
                );
        AbilityDefinition basic =
                damageAbility(
                        BASIC_ID,
                        80,
                        0,
                        0.0,
                        2.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.15
                                )
                        ),
                        resolver
                );

        PriorityAbilityPolicy playerRotation =
                new PriorityAbilityPolicy(
                        List.of(
                                bolt,
                                fallback
                        )
                );
        PriorityAbilityPolicy eliteRotation =
                new PriorityAbilityPolicy(
                        List.of(
                                heavy,
                                basic
                        )
                );

        long fightStart = simulator.now();

        while (!elite.resources().require(HEALTH).isEmpty()
                && !player.resources().require(HEALTH).isEmpty()
                && simulator.now() - fightStart < MAX_FIGHT_TICKS) {
            boolean heavyReadyToInterrupt =
                    eliteDriver.casts()
                            .activeCast()
                            .filter(cast ->
                                    cast.ability()
                                            .id()
                                            .equals(HEAVY_ID)
                            )
                            .filter(cast ->
                                    simulator.now()
                                            - cast.startedAtTick()
                                            >= INTERRUPT_REACTION_TICKS
                            )
                            .isPresent();

            if (playerDriver.casts().activeCast().isEmpty()) {
                if (heavyReadyToInterrupt) {
                    activateRequired(
                            playerDriver,
                            interrupt,
                            elite,
                            "elite interrupt"
                    );
                }

                if (playerDriver.casts().activeCast().isEmpty()) {
                    playerRotation.choose(
                            playerDriver.casts(),
                            elite,
                            simulator.now()
                    ).ifPresent(ability ->
                            activateRequired(
                                    playerDriver,
                                    ability,
                                    elite,
                                    "player elite rotation"
                            )
                    );
                }
            }

            if (eliteDriver.casts().activeCast().isEmpty()) {
                eliteRotation.choose(
                        eliteDriver.casts(),
                        player,
                        simulator.now()
                ).ifPresent(ability ->
                        activateRequired(
                                eliteDriver,
                                ability,
                                player,
                                "elite rotation"
                        )
                );
            }

            playerDriver.advanceAndTick(STEP_TICKS);
            eliteDriver.tickNow();
        }

        if (!player.resources().require(HEALTH).isEmpty()
                && !elite.resources().require(HEALTH).isEmpty()) {
            throw new IllegalStateException(
                    "elite calibration exceeded "
                            + MAX_FIGHT_TICKS
                            + " ticks"
            );
        }

        return new DetailedResult(
                simulator.report(),
                simulator.events(),
                player.resources()
                        .require(HEALTH)
                        .current(),
                player.resources()
                        .require(MANA)
                        .current()
        );
    }

    private static AbilityDefinition interruptAbility(
            LookupCombatCastControlGateway castControl
    ) {
        return new AbilityDefinition(
                INTERRUPT_ID,
                AbilityCastKind.INSTANT,
                0,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                aliveConditions(),
                new EffectSequence(
                        List.of(
                                new InterruptEffect(
                                        EffectRecipient.TARGET,
                                        castControl
                                )
                        )
                )
        );
    }

    private static AbilityDefinition damageAbility(
            RpgId id,
            long castTicks,
            long cooldownTicks,
            double manaCost,
            double authoredBase,
            RpgId school,
            RpgId resolutionProfile,
            CombatPowerScaling scaling,
            ProfiledCombatResolver resolver
    ) {
        return new AbilityDefinition(
                id,
                AbilityCastKind.TIMED,
                castTicks,
                OptionalLong.empty(),
                cooldownTicks,
                0,
                manaCost == 0.0
                        ? List.of()
                        : List.of(
                                new AbilityCost(
                                        MANA,
                                        manaCost
                                )
                        ),
                aliveConditions(),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        id,
                                        school,
                                        resolutionProfile,
                                        scaling,
                                        authoredBase,
                                        resolver
                                )
                        )
                )
        );
    }

    private static void activateRequired(
            HeadlessAbilityDriver driver,
            AbilityDefinition ability,
            CombatActor target,
            String label
    ) {
        var activation =
                driver.activate(
                        ability,
                        target
                );

        if (!activation.accepted()) {
            throw new IllegalStateException(
                    label
                            + " rejected: "
                            + activation.validation()
            );
        }
    }

    private static dev.worldrpg.combat.condition.Condition<
            dev.worldrpg.combat.ability.AbilityContext
            > aliveConditions() {
        return Conditions.all(
                TargetConditions.requireSourceAlive(),
                TargetConditions.requireTargetAlive()
        );
    }

    private static AbilityObservationProvider aliveObservationProvider() {
        return (source, target) ->
                TargetObservation.observed(
                        source.id().equals(target.id()),
                        true,
                        alive(source),
                        alive(target),
                        true,
                        OptionalDouble.empty()
                );
    }

    private static boolean alive(CombatActor actor) {
        return actor.resources()
                .find(HEALTH)
                .map(pool -> !pool.isEmpty())
                .orElse(false);
    }

    public record DetailedResult(
            CombatSimulationReport report,
            List<CombatEvent> events,
            double playerHealthRemaining,
            double playerManaRemaining
    ) {
        public DetailedResult {
            events = List.copyOf(events);
        }
    }
}
