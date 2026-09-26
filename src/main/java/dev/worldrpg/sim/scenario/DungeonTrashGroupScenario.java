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
import dev.worldrpg.combat.condition.ResourceConditions;
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
 * First small-party dungeon trash calibration.
 *
 * <p>Threat is intentionally not simulated yet. Enemy target selection is
 * fixed to the tank until the production threat table is connected.</p>
 */
public final class DungeonTrashGroupScenario
        implements CombatSimulationScenario {
    public static final int LEVEL = 20;
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    public static final CombatActorId TANK_ID =
            new CombatActorId(1);
    public static final CombatActorId HEALER_ID =
            new CombatActorId(2);
    public static final CombatActorId DPS_ID =
            new CombatActorId(3);
    public static final CombatActorId GUARD_ID =
            new CombatActorId(10);
    public static final CombatActorId SKIRMISHER_ID =
            new CombatActorId(11);
    public static final CombatActorId CASTER_ID =
            new CombatActorId(12);

    public static final RpgId DPS_FALLBACK_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_fallback"
            );
    private static final RpgId TANK_STRIKE_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_tank_strike"
            );
    private static final RpgId HEAL_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_heal"
            );
    private static final RpgId DPS_BOLT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_bolt"
            );
    private static final RpgId INTERRUPT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_interrupt"
            );
    private static final RpgId GUARD_ATTACK_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_guard_attack"
            );
    private static final RpgId SKIRMISHER_ATTACK_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_skirmisher_attack"
            );
    private static final RpgId CASTER_BOLT_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_caster_bolt"
            );
    private static final RpgId CASTER_HEAVY_ID =
            RpgId.parse(
                    "world_rpg:ability/calibration/dungeon_caster_heavy"
            );

    private static final long STEP_TICKS = 10L;
    private static final long MAX_TICKS = 3_600L;
    private static final long INTERRUPT_REACTION_TICKS = 20L;

    @Override
    public CombatSimulationReport run(long seed) {
        return runDetailed(seed).report();
    }

    public DetailedResult runDetailed(long seed) {
        CombatActor tank = new CombatActor(TANK_ID);
        CombatActor healer = new CombatActor(HEALER_ID);
        CombatActor dps = new CombatActor(DPS_ID);
        CombatActor guard = new CombatActor(GUARD_ID);
        CombatActor skirmisher = new CombatActor(SKIRMISHER_ID);
        CombatActor caster = new CombatActor(CASTER_ID);

        double referenceHealth =
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .valueAt(LEVEL);
        double referenceResource =
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE
                        .valueAt(LEVEL);
        double referencePower =
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .valueAt(LEVEL);

        addHealth(tank, referenceHealth * 2.0);
        addHealth(healer, referenceHealth);
        addHealth(dps, referenceHealth);
        addHealth(guard, referenceHealth * 2.0);
        addHealth(skirmisher, referenceHealth);
        addHealth(caster, referenceHealth);

        healer.resources().add(
                MANA,
                referenceResource,
                referenceResource
        );
        dps.resources().add(
                MANA,
                referenceResource,
                referenceResource
        );

        tank.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower
        );
        tank.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.03
        );
        tank.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(LEVEL)
                        * 1.50
        );

        healer.stats().setBase(
                CombatMathStats.HEALING_POWER,
                referencePower
        );
        healer.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.05
        );

        dps.stats().setBase(
                CombatMathStats.SPELL_POWER,
                referencePower
        );
        dps.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                0.05
        );

        guard.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower
        );
        skirmisher.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                referencePower * 0.80
        );
        caster.stats().setBase(
                CombatMathStats.SPELL_POWER,
                referencePower * 0.80
        );

        for (CombatActor enemy :
                List.of(guard, skirmisher, caster)) {
            enemy.stats().setBase(
                    CombatMathStats.CRIT_CHANCE,
                    0.03
            );
            enemy.stats().setBase(
                    CombatMathStats.ARMOR,
                    WorldRpgDefenseDraft.referenceArmor(LEVEL)
            );
            enemy.stats().setBase(
                    CombatMathStats.ARCANE_RESISTANCE,
                    WorldRpgDefenseDraft.referenceResistance(LEVEL)
            );
        }

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

        HeadlessAbilityDriver tankDriver =
                driver(simulator, tank, observations);
        HeadlessAbilityDriver healerDriver =
                driver(simulator, healer, observations);
        HeadlessAbilityDriver dpsDriver =
                driver(simulator, dps, observations);
        HeadlessAbilityDriver guardDriver =
                driver(simulator, guard, observations);
        HeadlessAbilityDriver skirmisherDriver =
                driver(simulator, skirmisher, observations);
        HeadlessAbilityDriver casterDriver =
                driver(simulator, caster, observations);

        Map<CombatActorId, dev.worldrpg.combat.cast.CastController>
                controllers = Map.of(
                        tank.id(), tankDriver.casts(),
                        healer.id(), healerDriver.casts(),
                        dps.id(), dpsDriver.casts(),
                        guard.id(), guardDriver.casts(),
                        skirmisher.id(), skirmisherDriver.casts(),
                        caster.id(), casterDriver.casts()
                );

        LookupCombatCastControlGateway castControl =
                new LookupCombatCastControlGateway(
                        id -> Optional.ofNullable(
                                controllers.get(id)
                        )
                );

        AbilityDefinition tankStrike =
                damageAbility(
                        TANK_STRIKE_ID,
                        60,
                        0,
                        0.0,
                        4.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.20
                                )
                        ),
                        resolver
                );

        AbilityDefinition heal =
                healAbility(resolver);

        AbilityDefinition dpsBolt =
                damageAbility(
                        DPS_BOLT_ID,
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

        AbilityDefinition dpsFallback =
                damageAbility(
                        DPS_FALLBACK_ID,
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

        AbilityDefinition interrupt =
                interruptAbility(castControl);

        AbilityDefinition guardAttack =
                damageAbility(
                        GUARD_ATTACK_ID,
                        70,
                        0,
                        0.0,
                        4.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.25
                                )
                        ),
                        resolver
                );

        AbilityDefinition skirmisherAttack =
                damageAbility(
                        SKIRMISHER_ATTACK_ID,
                        60,
                        0,
                        0.0,
                        3.0,
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.20
                                )
                        ),
                        resolver
                );

        AbilityDefinition casterHeavy =
                damageAbility(
                        CASTER_HEAVY_ID,
                        80,
                        400,
                        0.0,
                        25.0,
                        CombatSchools.SHADOW.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        CombatPowerScaling.none(),
                        resolver
                );

        AbilityDefinition casterBolt =
                damageAbility(
                        CASTER_BOLT_ID,
                        80,
                        0,
                        0.0,
                        5.0,
                        CombatSchools.SHADOW.id(),
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.SPELL_POWER,
                                        0.30
                                )
                        ),
                        resolver
                );

        PriorityAbilityPolicy tankPolicy =
                new PriorityAbilityPolicy(
                        List.of(tankStrike)
                );
        PriorityAbilityPolicy healerPolicy =
                new PriorityAbilityPolicy(
                        List.of(heal)
                );
        PriorityAbilityPolicy dpsPolicy =
                new PriorityAbilityPolicy(
                        List.of(dpsBolt, dpsFallback)
                );
        PriorityAbilityPolicy casterPolicy =
                new PriorityAbilityPolicy(
                        List.of(casterHeavy, casterBolt)
                );

        while (anyAlive(guard, skirmisher, caster)
                && !tank.resources().require(HEALTH).isEmpty()
                && simulator.now() < MAX_TICKS) {
            CombatActor tankTarget =
                    firstAlive(
                            guard,
                            skirmisher,
                            caster
                    );
            CombatActor dpsTarget =
                    firstAlive(
                            caster,
                            skirmisher,
                            guard
                    );

            if (tankDriver.casts().activeCast().isEmpty()
                    && tankTarget != null) {
                activatePolicy(
                        tankPolicy,
                        tankDriver,
                        tankTarget,
                        simulator.now(),
                        "tank rotation"
                );
            }

            if (healerDriver.casts().activeCast().isEmpty()) {
                activatePolicy(
                        healerPolicy,
                        healerDriver,
                        tank,
                        simulator.now(),
                        "healer rotation"
                );
            }

            boolean casterHeavyReadyToInterrupt =
                    !caster.resources().require(HEALTH).isEmpty()
                            && casterDriver.casts()
                                    .activeCast()
                                    .filter(active ->
                                            active.ability()
                                                    .id()
                                                    .equals(
                                                            CASTER_HEAVY_ID
                                                    )
                                    )
                                    .filter(active ->
                                            simulator.now()
                                                    - active.startedAtTick()
                                                    >= INTERRUPT_REACTION_TICKS
                                    )
                                    .isPresent();

            if (dpsDriver.casts().activeCast().isEmpty()
                    && dpsTarget != null) {
                if (casterHeavyReadyToInterrupt) {
                    activateRequired(
                            dpsDriver,
                            interrupt,
                            caster,
                            "dungeon interrupt"
                    );
                }

                if (dpsDriver.casts().activeCast().isEmpty()) {
                    activatePolicy(
                            dpsPolicy,
                            dpsDriver,
                            dpsTarget,
                            simulator.now(),
                            "dps rotation"
                    );
                }
            }

            if (!guard.resources().require(HEALTH).isEmpty()
                    && guardDriver.casts().activeCast().isEmpty()) {
                activateRequired(
                        guardDriver,
                        guardAttack,
                        tank,
                        "guard attack"
                );
            }

            if (!skirmisher.resources().require(HEALTH).isEmpty()
                    && skirmisherDriver.casts().activeCast().isEmpty()) {
                activateRequired(
                        skirmisherDriver,
                        skirmisherAttack,
                        tank,
                        "skirmisher attack"
                );
            }

            if (!caster.resources().require(HEALTH).isEmpty()
                    && casterDriver.casts().activeCast().isEmpty()) {
                activatePolicy(
                        casterPolicy,
                        casterDriver,
                        tank,
                        simulator.now(),
                        "caster rotation"
                );
            }

            dpsDriver.advanceAndTick(STEP_TICKS);
            tankDriver.tickNow();
            healerDriver.tickNow();
            guardDriver.tickNow();
            skirmisherDriver.tickNow();
            casterDriver.tickNow();
        }

        if (!tank.resources().require(HEALTH).isEmpty()
                && anyAlive(guard, skirmisher, caster)) {
            throw new IllegalStateException(
                    "dungeon trash calibration exceeded "
                            + MAX_TICKS
                            + " ticks"
            );
        }

        return new DetailedResult(
                simulator.report(),
                simulator.events(),
                tank.resources()
                        .require(HEALTH)
                        .current(),
                healer.resources()
                        .require(MANA)
                        .current(),
                dps.resources()
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

    private static void activatePolicy(
            PriorityAbilityPolicy policy,
            HeadlessAbilityDriver driver,
            CombatActor target,
            long gameTick,
            String label
    ) {
        policy.choose(
                driver.casts(),
                target,
                gameTick
        ).ifPresent(ability ->
                activateRequired(
                        driver,
                        ability,
                        target,
                        label
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

    private static HeadlessAbilityDriver driver(
            HeadlessCombatSimulator simulator,
            CombatActor actor,
            AbilityObservationProvider observations
    ) {
        return new HeadlessAbilityDriver(
                simulator,
                actor,
                new CooldownBook(),
                observations
        );
    }

    private static void addHealth(
            CombatActor actor,
            double amount
    ) {
        actor.resources().add(
                HEALTH,
                amount,
                amount
        );
    }

    private static boolean anyAlive(
            CombatActor... actors
    ) {
        for (CombatActor actor : actors) {
            if (alive(actor)) {
                return true;
            }
        }
        return false;
    }

    private static CombatActor firstAlive(
            CombatActor... actors
    ) {
        for (CombatActor actor : actors) {
            if (alive(actor)) {
                return actor;
            }
        }
        return null;
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
            double tankHealthRemaining,
            double healerManaRemaining,
            double dpsManaRemaining
    ) {
        public DetailedResult {
            events = List.copyOf(events);
        }
    }
}
