package dev.worldrpg.sim.scenario;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastInterruptionReason;
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
import dev.worldrpg.sim.CombatSimulationScenario;
import dev.worldrpg.sim.HeadlessAbilityDriver;
import dev.worldrpg.sim.HeadlessCombatSimulator;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Routine same-level caster that attempts one long self-heal.
 *
 * <p>The scenario differs only by whether the player reacts to that cast.</p>
 */
public final class CasterHealDecisionScenario
        implements CombatSimulationScenario {
    public enum Decision {
        INTERRUPT,
        IGNORE
    }

    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    public static final CombatActorId PLAYER_ID =
            new CombatActorId(1);
    public static final CombatActorId ENEMY_ID =
            new CombatActorId(2);

    private static final int LEVEL = 20;
    private static final long STEP_TICKS = 10L;
    private static final long MAX_FIGHT_TICKS = 2_000L;
    private static final long INTERRUPT_REACTION_TICKS = 20L;
    private static final double HEAL_THRESHOLD = 0.70;

    private static final RpgId BOLT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/caster_bolt"
            );
    private static final RpgId STRIKE_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/caster_strike"
            );
    private static final RpgId HEAL_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/caster_heal"
            );

    private final Decision decision;

    public CasterHealDecisionScenario(Decision decision) {
        this.decision =
                java.util.Objects.requireNonNull(
                        decision,
                        "decision"
                );
    }

    @Override
    public CombatSimulationReport run(long seed) {
        CombatActor player =
                new CombatActor(PLAYER_ID);
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
                referencePower * 0.65
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

        AbilityDefinition playerBolt =
                damageAbility(
                        BOLT_ID,
                        50,
                        4.0,
                        10.0,
                        CombatSchools.ARCANE.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        resolver
                );

        AbilityDefinition enemyStrike =
                damageAbility(
                        STRIKE_ID,
                        60,
                        0.0,
                        5.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        resolver
                );

        AbilityDefinition enemyHeal =
                healAbility(
                        resolver
                );

        double enemyMaximumHealth =
                enemy.resources()
                        .require(HEALTH)
                        .maximum();

        boolean healAttempted = false;
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
                            "player caster-calibration ability rejected: "
                                    + activation.validation()
                    );
                }
            }

            if (enemyDriver.casts().activeCast().isEmpty()) {
                boolean shouldHeal =
                        !healAttempted
                                && enemy.resources()
                                        .require(HEALTH)
                                        .current()
                                <= enemyMaximumHealth
                                        * HEAL_THRESHOLD;

                AbilityDefinition next =
                        shouldHeal
                                ? enemyHeal
                                : enemyStrike;
                CombatActor target =
                        shouldHeal
                                ? enemy
                                : player;

                var activation =
                        enemyDriver.activate(
                                next,
                                target
                        );

                if (!activation.accepted()) {
                    throw new IllegalStateException(
                            "enemy caster-calibration ability rejected: "
                                    + activation.validation()
                    );
                }

                if (shouldHeal) {
                    healAttempted = true;
                }
            }

            if (decision == Decision.INTERRUPT
                    && enemyDriver.casts()
                            .activeCast()
                            .filter(cast ->
                                    cast.ability()
                                            .id()
                                            .equals(HEAL_ID)
                            )
                            .filter(cast ->
                                    simulator.now()
                                            - cast.startedAtTick()
                                            >= INTERRUPT_REACTION_TICKS
                            )
                            .isPresent()) {
                enemyDriver.interrupt(
                        CastInterruptionReason.INTERRUPT
                );
            }

            playerDriver.advanceAndTick(STEP_TICKS);
            enemyDriver.tickNow();
        }

        if (player.resources().require(HEALTH).isEmpty()) {
            throw new IllegalStateException(
                    "caster calibration enemy defeated the reference player"
            );
        }

        if (!enemy.resources().require(HEALTH).isEmpty()) {
            throw new IllegalStateException(
                    "caster calibration exceeded "
                            + MAX_FIGHT_TICKS
                            + " ticks"
            );
        }

        return simulator.report();
    }

    private static AbilityDefinition damageAbility(
            RpgId id,
            long castTicks,
            double manaCost,
            double authoredBase,
            RpgId school,
            RpgId resolutionProfile,
            ProfiledCombatResolver resolver
    ) {
        return new AbilityDefinition(
                id,
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
                aliveConditions(),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        id,
                                        school,
                                        resolutionProfile,
                                        authoredBase,
                                        resolver
                                )
                        )
                )
        );
    }

    private static AbilityDefinition healAbility(
            ProfiledCombatResolver resolver
    ) {
        return new AbilityDefinition(
                HEAL_ID,
                AbilityCastKind.TIMED,
                80,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                aliveConditions(),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.SOURCE,
                                        CombatMagnitudeKind.HEALING,
                                        HEAL_ID,
                                        CombatSchools.HOLY.id(),
                                        CombatResolutionProfileIds.GUARANTEED,
                                        60.0,
                                        resolver
                                )
                        )
                )
        );
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
}
