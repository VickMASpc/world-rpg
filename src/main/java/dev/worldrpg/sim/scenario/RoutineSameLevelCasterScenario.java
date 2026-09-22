package dev.worldrpg.sim.scenario;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
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
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.target.TargetConditions;
import dev.worldrpg.combat.target.TargetObservation;
import dev.worldrpg.progression.WorldRpgMagnitudeDraft;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.CombatSimulationScenario;
import dev.worldrpg.sim.HeadlessAbilityDriver;
import dev.worldrpg.sim.HeadlessCombatSimulator;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * First representative P4.4 scenario.
 *
 * <p>This is a calibration fixture, not production class/enemy content.</p>
 */
public final class RoutineSameLevelCasterScenario
        implements CombatSimulationScenario {
    public static final int LEVEL = 20;
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    private static final long STEP_TICKS = 10L;
    private static final long MAX_TICKS = 2_000L;

    @Override
    public CombatSimulationReport run(long seed) {
        CombatActor player =
                new CombatActor(new CombatActorId(1));
        CombatActor enemy =
                new CombatActor(new CombatActorId(2));

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
        enemy.resources().add(
                HEALTH,
                referenceHealth * 0.85,
                referenceHealth * 0.85
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

        enemy.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower * 0.70
        );
        enemy.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.02
        );
        enemy.stats().setBase(
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
                        new dev.worldrpg.combat.cooldown.CooldownBook(),
                        observations
                );
        HeadlessAbilityDriver enemyDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        enemy,
                        new dev.worldrpg.combat.cooldown.CooldownBook(),
                        observations
                );

        AbilityDefinition playerBolt =
                timedDamageAbility(
                        "world_rpg:ability/calibration/routine_bolt",
                        50,
                        4.0,
                        10.0,
                        CombatSchools.ARCANE.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        resolver
                );

        AbilityDefinition enemyStrike =
                timedDamageAbility(
                        "world_rpg:ability/calibration/routine_strike",
                        60,
                        0.0,
                        5.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        resolver
                );

        while (!enemy.resources().require(HEALTH).isEmpty()
                && !player.resources().require(HEALTH).isEmpty()
                && simulator.now() < MAX_TICKS) {
            if (playerDriver.casts().activeCast().isEmpty()) {
                var activation =
                        playerDriver.activate(
                                playerBolt,
                                enemy
                        );

                if (!activation.accepted()) {
                    throw new IllegalStateException(
                            "player calibration ability rejected: "
                                    + activation.validation()
                    );
                }
            }

            if (enemyDriver.casts().activeCast().isEmpty()) {
                var activation =
                        enemyDriver.activate(
                                enemyStrike,
                                player
                        );

                if (!activation.accepted()) {
                    throw new IllegalStateException(
                            "enemy calibration ability rejected: "
                                    + activation.validation()
                    );
                }
            }

            playerDriver.advanceAndTick(STEP_TICKS);
            enemyDriver.tickNow();
        }

        if (player.resources().require(HEALTH).isEmpty()) {
            throw new IllegalStateException(
                    "routine calibration enemy defeated the reference player"
            );
        }

        if (!enemy.resources().require(HEALTH).isEmpty()) {
            throw new IllegalStateException(
                    "routine calibration exceeded "
                            + MAX_TICKS
                            + " ticks"
            );
        }

        return simulator.report();
    }

    private static AbilityDefinition timedDamageAbility(
            String id,
            long castTicks,
            double manaCost,
            double authoredBase,
            RpgId school,
            RpgId resolutionProfile,
            ProfiledCombatResolver resolver
    ) {
        RpgId abilityId = RpgId.parse(id);

        return new AbilityDefinition(
                abilityId,
                AbilityCastKind.TIMED,
                castTicks,
                OptionalLong.empty(),
                0,
                0,
                manaCost == 0.0
                        ? List.of()
                        : List.of(
                                new AbilityCost(
                                        MANA,
                                        manaCost
                                )
                        ),
                Conditions.all(
                        TargetConditions.requireSourceAlive(),
                        TargetConditions.requireTargetAlive()
                ),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        abilityId,
                                        school,
                                        resolutionProfile,
                                        authoredBase,
                                        resolver
                                )
                        )
                )
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
}
