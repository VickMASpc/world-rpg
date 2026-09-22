package dev.worldrpg.sim;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.CombatMagnitudeEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.CombatSchools;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.math.SeededCombatRollSource;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadlessAbilityDriverTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void timedAbilityUsesSharedClockAndProductionCastLifecycle() {
        Fixture fixture = fixture(1L);

        AbilityDefinition bolt =
                ability(
                        "world_rpg:ability/test/sim_bolt",
                        AbilityCastKind.TIMED,
                        20,
                        OptionalLong.empty(),
                        20.0,
                        30.0,
                        fixture.resolver
                );

        var activation =
                fixture.driver.activate(
                        bolt,
                        fixture.target
                );

        assertTrue(activation.accepted());
        assertEquals(
                80.0,
                fixture.caster.resources()
                        .require(MANA)
                        .current(),
                0.0
        );
        assertEquals(
                100.0,
                fixture.target.resources()
                        .require(HEALTH)
                        .current(),
                0.0
        );

        fixture.driver.advanceAndTick(19);

        assertEquals(19L, fixture.simulator.now());
        assertEquals(
                100.0,
                fixture.target.resources()
                        .require(HEALTH)
                        .current(),
                0.0
        );
        assertEquals(
                0L,
                fixture.simulator.report()
                        .damageResolutions()
        );

        fixture.driver.advanceAndTick(1);

        assertEquals(20L, fixture.simulator.now());
        assertEquals(
                70.0,
                fixture.target.resources()
                        .require(HEALTH)
                        .current(),
                0.0
        );
        assertEquals(
                1L,
                fixture.simulator.report()
                        .damageResolutions()
        );
        assertEquals(
                30.0,
                fixture.simulator.report()
                        .damageApplied(),
                0.0
        );
    }

    @Test
    void channelCatchUpUsesP3ScheduledTicksWhenSimulatorJumpsForward() {
        Fixture fixture = fixture(2L);

        AbilityDefinition channel =
                ability(
                        "world_rpg:ability/test/sim_channel",
                        AbilityCastKind.CHANNEL,
                        60,
                        OptionalLong.of(20),
                        0.0,
                        5.0,
                        fixture.resolver
                );

        assertTrue(
                fixture.driver.activate(
                        channel,
                        fixture.target
                ).accepted()
        );

        fixture.driver.advanceAndTick(60);

        assertTrue(
                fixture.driver.casts()
                        .activeCast()
                        .isEmpty()
        );
        assertEquals(
                85.0,
                fixture.target.resources()
                        .require(HEALTH)
                        .current(),
                0.0
        );
        assertEquals(
                3L,
                fixture.simulator.report()
                        .damageResolutions()
        );
        assertEquals(
                15.0,
                fixture.simulator.report()
                        .damageApplied(),
                0.0
        );
    }

    private static AbilityDefinition ability(
            String id,
            AbilityCastKind kind,
            long duration,
            OptionalLong channelInterval,
            double manaCost,
            double damage,
            ProfiledCombatResolver resolver
    ) {
        RpgId abilityId = RpgId.parse(id);

        return new AbilityDefinition(
                abilityId,
                kind,
                duration,
                channelInterval,
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
                Conditions.all(List.of()),
                new EffectSequence(
                        List.of(
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        abilityId,
                                        CombatSchools.ARCANE.id(),
                                        CombatResolutionProfileIds.GUARANTEED,
                                        damage,
                                        resolver
                                )
                        )
                )
        );
    }

    private static Fixture fixture(long seed) {
        CombatActor caster =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        caster.resources().add(
                MANA,
                100.0,
                100.0
        );
        target.resources().add(
                HEALTH,
                100.0,
                100.0
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        new CombatMathProfile(
                                HEALTH,
                                0.0,
                                0.0,
                                0.0,
                                2.0,
                                1.0,
                                100.0,
                                0.75
                        ),
                        new SeededCombatRollSource(seed)
                );

        HeadlessCombatSimulator simulator =
                new HeadlessCombatSimulator(resolver);

        HeadlessAbilityDriver driver =
                new HeadlessAbilityDriver(
                        simulator,
                        caster
                );

        return new Fixture(
                caster,
                target,
                resolver,
                simulator,
                driver
        );
    }

    private record Fixture(
            CombatActor caster,
            CombatActor target,
            ProfiledCombatResolver resolver,
            HeadlessCombatSimulator simulator,
            HeadlessAbilityDriver driver
    ) {
    }
}
