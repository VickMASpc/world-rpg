package dev.worldrpg.sim.scenario;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
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
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.target.TargetConditions;
import dev.worldrpg.combat.target.TargetObservation;
import dev.worldrpg.progression.WorldRpgMagnitudeDraft;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.HeadlessAbilityDriver;
import dev.worldrpg.sim.HeadlessCombatSimulator;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Reusable calibration session whose player/RNG/clock/resources persist across
 * routine encounters.
 */
final class RoutineCasterCalibrationSession {
    static final int LEVEL = 20;
    static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    private static final long STEP_TICKS = 10L;
    private static final long MAX_FIGHT_TICKS = 2_000L;

    private final CombatActor player;
    private final ProfiledCombatResolver resolver;
    private final HeadlessCombatSimulator simulator;
    private final HeadlessAbilityDriver playerDriver;
    private final AbilityObservationProvider observations;
    private final AbilityDefinition playerBolt;
    private final AbilityDefinition enemyStrike;
    private long nextEnemyId = 2L;

    RoutineCasterCalibrationSession(long seed) {
        player = new CombatActor(new CombatActorId(1));

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

        resolver =
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

        simulator = new HeadlessCombatSimulator(resolver);
        observations = aliveObservationProvider();

        playerDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        player,
                        new CooldownBook(),
                        observations
                );

        playerBolt =
                timedDamageAbility(
                        "world_rpg:ability/calibration/routine_bolt",
                        50,
                        4.0,
                        10.0,
                        CombatSchools.ARCANE.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL
                );

        enemyStrike =
                timedDamageAbility(
                        "world_rpg:ability/calibration/routine_strike",
                        60,
                        0.0,
                        5.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON
                );
    }

    void fightRoutineEnemy() {
        CombatActor enemy = createEnemy();
        HeadlessAbilityDriver enemyDriver =
                new HeadlessAbilityDriver(
                        simulator,
                        enemy,
                        new CooldownBook(),
                        observations
                );

        long fightStart = simulator.now();

        while (!enemy.resources().require(HEALTH).isEmpty()
                && !player.resources().require(HEALTH).isEmpty()
                && simulator.now() - fightStart < MAX_FIGHT_TICKS) {
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
                            + MAX_FIGHT_TICKS
                            + " ticks"
            );
        }
    }

    CombatSimulationReport report() {
        return simulator.report();
    }

    double playerHealth() {
        return player.resources().require(HEALTH).current();
    }

    double playerMana() {
        return player.resources().require(MANA).current();
    }

    private CombatActor createEnemy() {
        CombatActor enemy =
                new CombatActor(
                        new CombatActorId(nextEnemyId++)
                );

        double referenceHealth =
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .valueAt(LEVEL);
        double referencePower =
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .valueAt(LEVEL);

        enemy.resources().add(
                HEALTH,
                referenceHealth * 0.85,
                referenceHealth * 0.85
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

        return enemy;
    }

    private AbilityDefinition timedDamageAbility(
            String id,
            long castTicks,
            double manaCost,
            double authoredBase,
            RpgId school,
            RpgId resolutionProfile
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

    private AbilityObservationProvider aliveObservationProvider() {
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

    private boolean alive(CombatActor actor) {
        return actor.resources()
                .find(HEALTH)
                .map(pool -> !pool.isEmpty())
                .orElse(false);
    }
}
