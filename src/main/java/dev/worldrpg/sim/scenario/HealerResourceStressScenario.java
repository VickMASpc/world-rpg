package dev.worldrpg.sim.scenario;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.condition.ResourceConditions;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.effect.CombatMagnitudeEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
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
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Fixed-duration healer resource-pressure scenario.
 *
 * <p>The healer does not spam by simulator rule. Its heal becomes legal only
 * when the ally health pool reaches the authored threshold condition.</p>
 */
public final class HealerResourceStressScenario
        implements CombatSimulationScenario {
    public static final int LEVEL = 20;
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    public static final CombatActorId HEALER_ID =
            new CombatActorId(1);
    public static final CombatActorId ALLY_ID =
            new CombatActorId(2);
    public static final CombatActorId ENEMY_ID =
            new CombatActorId(3);

    public static final RpgId HEAL_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/stress_heal"
            );
    public static final RpgId ATTACK_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/stress_attack"
            );

    private static final long STEP_TICKS = 10L;
    private static final long PRESSURE_TICKS = 5_000L;

    @Override
    public CombatSimulationReport run(long seed) {
        return runDetailed(seed).report();
    }

    public DetailedResult runDetailed(long seed) {
        CombatActor healer =
                new CombatActor(HEALER_ID);
        CombatActor ally =
                new CombatActor(ALLY_ID);
        CombatActor enemy =
                new CombatActor(ENEMY_ID);

        double referenceHealth =
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .valueAt(LEVEL);
        double referenceResource =
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE
                        .valueAt(LEVEL);
        double referencePower =
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .valueAt(LEVEL);

        healer.resources().add(
                HEALTH,
                referenceHealth,
                referenceHealth
        );
        healer.resources().add(
                MANA,
                referenceResource,
                referenceResource
        );
        ally.resources().add(
                HEALTH,
                referenceHealth * 2.0,
                referenceHealth * 2.0
        );
        enemy.resources().add(
                HEALTH,
                referenceHealth,
                referenceHealth
        );

        healer.stats().setBase(
                CombatMathStats.HEALING_POWER,
                referencePower
        );
        healer.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.05
        );

        ally.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(LEVEL)
                        * 1.50
        );

        enemy.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower * 1.25
        );
        enemy.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.04
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

        HeadlessAbilityDriver healerDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        healer,
                        new CooldownBook(),
                        observations
                );
        HeadlessAbilityDriver enemyDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        enemy,
                        new CooldownBook(),
                        observations
                );

        AbilityDefinition heal =
                healAbility(resolver);
        AbilityDefinition attack =
                attackAbility(resolver);

        PriorityAbilityPolicy healerPolicy =
                new PriorityAbilityPolicy(
                        List.of(heal)
                );

        while (simulator.now() < PRESSURE_TICKS
                && !ally.resources().require(HEALTH).isEmpty()) {
            if (healerDriver.casts().activeCast().isEmpty()) {
                healerPolicy.choose(
                        healerDriver.casts(),
                        ally,
                        simulator.now()
                ).ifPresent(ability ->
                        activateRequired(
                                healerDriver,
                                ability,
                                ally,
                                "healer rotation"
                        )
                );
            }

            if (enemyDriver.casts().activeCast().isEmpty()) {
                activateRequired(
                        enemyDriver,
                        attack,
                        ally,
                        "pressure attack"
                );
            }

            healerDriver.advanceAndTick(STEP_TICKS);
            enemyDriver.tickNow();
        }

        return new DetailedResult(
                simulator.report(),
                ally.resources()
                        .require(HEALTH)
                        .current(),
                healer.resources()
                        .require(MANA)
                        .current()
        );
    }

    private static AbilityDefinition healAbility(
            ProfiledCombatResolver resolver
    ) {
        return new AbilityDefinition(
                HEAL_ID,
                AbilityCastKind.TIMED,
                50,
                OptionalLong.empty(),
                0,
                0,
                List.of(
                        new AbilityCost(
                                MANA,
                                10.0
                        )
                ),
                Conditions.all(
                        TargetConditions.requireSourceAlive(),
                        TargetConditions.requireTargetAlive(),
                        ResourceConditions.targetAtOrBelowFraction(
                                HEALTH,
                                0.70
                        )
                ),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.HEALING,
                                        HEAL_ID,
                                        CombatSchools.HOLY.id(),
                                        CombatResolutionProfileIds.GUARANTEED,
                                        CombatPowerScaling.explicit(
                                                new CombatPowerTerm(
                                                        CombatMathStats.HEALING_POWER,
                                                        0.60
                                                )
                                        ),
                                        25.0,
                                        resolver
                                )
                        )
                )
        );
    }

    private static AbilityDefinition attackAbility(
            ProfiledCombatResolver resolver
    ) {
        return new AbilityDefinition(
                ATTACK_ID,
                AbilityCastKind.TIMED,
                50,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                Conditions.all(
                        TargetConditions.requireSourceAlive(),
                        TargetConditions.requireTargetAlive()
                ),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        ATTACK_ID,
                                        CombatSchools.PHYSICAL.id(),
                                        CombatResolutionProfileIds.DIRECT_WEAPON,
                                        CombatPowerScaling.explicit(
                                                new CombatPowerTerm(
                                                        CombatMathStats.ATTACK_POWER,
                                                        0.40
                                                )
                                        ),
                                        6.0,
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
            double allyHealthRemaining,
            double healerManaRemaining
    ) {
    }
}
