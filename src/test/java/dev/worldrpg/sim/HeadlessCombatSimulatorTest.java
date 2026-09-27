package dev.worldrpg.sim;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.CombatSchools;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.math.SeededCombatRollSource;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadlessCombatSimulatorTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    private static final CombatMathProfile PROFILE =
            new CombatMathProfile(
                    HEALTH,
                    0.0,
                    0.0,
                    0.0,
                    2.0,
                    1.0,
                    100.0,
                    0.75
            );

    @Test
    void elapsedTimeDoesNotResetPersistentActorState() {
        CombatActor attacker =
                new CombatActor(new CombatActorId(1));
        CombatActor player =
                new CombatActor(new CombatActorId(2));
        player.resources().add(
                HEALTH,
                100.0,
                100.0
        );

        HeadlessCombatSimulator simulator =
                new HeadlessCombatSimulator(
                        new ProfiledCombatResolver(
                                PROFILE,
                                new SeededCombatRollSource(1L)
                        )
                );

        simulator.resolve(
                CombatMagnitudeKind.DAMAGE,
                attacker,
                player,
                RpgId.parse("world_rpg:ability/test/hit"),
                CombatSchools.PHYSICAL.id(),
                30.0
        );

        assertEquals(
                70.0,
                player.resources().require(HEALTH).current(),
                0.0
        );

        simulator.advance(20_000L);

        assertEquals(
                70.0,
                player.resources().require(HEALTH).current(),
                0.0
        );

        simulator.resolve(
                CombatMagnitudeKind.DAMAGE,
                attacker,
                player,
                RpgId.parse("world_rpg:ability/test/hit"),
                CombatSchools.PHYSICAL.id(),
                30.0
        );

        assertEquals(
                40.0,
                player.resources().require(HEALTH).current(),
                0.0
        );

        CombatSimulationReport report = simulator.report();
        assertEquals(20_000L, report.elapsedTicks());
        assertEquals(2L, report.damageResolutions());
        assertEquals(60.0, report.damageApplied(), 0.0);
        assertEquals(0.0, report.healingApplied(), 0.0);
    }

    @Test
    void recoveryOnlyOccursWhenExplicitlyRequested() {
        CombatActor player =
                new CombatActor(new CombatActorId(1));
        player.resources().add(
                HEALTH,
                100.0,
                40.0
        );

        HeadlessCombatSimulator simulator =
                new HeadlessCombatSimulator(
                        new ProfiledCombatResolver(
                                PROFILE,
                                new SeededCombatRollSource(2L)
                        )
                );

        simulator.advance(5_000L);

        assertEquals(
                40.0,
                player.resources().require(HEALTH).current(),
                0.0
        );

        simulator.recover(
                player,
                HEALTH,
                15.0
        );

        assertEquals(
                55.0,
                player.resources().require(HEALTH).current(),
                0.0
        );
        assertEquals(
                15.0,
                simulator.report()
                        .explicitRecovery()
                        .get(HEALTH),
                0.0
        );
    }

    @Test
    void sameSeedAndInputsProduceSameEventJournal() {
        HeadlessCombatSimulator first =
                simulator(99L);
        HeadlessCombatSimulator second =
                simulator(99L);

        CombatActor firstSource =
                actor(1, 100.0);
        CombatActor firstTarget =
                actor(2, 100.0);
        CombatActor secondSource =
                actor(1, 100.0);
        CombatActor secondTarget =
                actor(2, 100.0);

        firstSource.stats().setBase(
                dev.worldrpg.combat.math.CombatMathStats.CRIT_CHANCE,
                0.5
        );
        secondSource.stats().setBase(
                dev.worldrpg.combat.math.CombatMathStats.CRIT_CHANCE,
                0.5
        );

        for (int i = 0; i < 3; i++) {
            first.resolve(
                    CombatMagnitudeKind.DAMAGE,
                    firstSource,
                    firstTarget,
                    RpgId.parse("world_rpg:ability/test/seeded"),
                    CombatSchools.ARCANE.id(),
                    10.0
            );
            second.resolve(
                    CombatMagnitudeKind.DAMAGE,
                    secondSource,
                    secondTarget,
                    RpgId.parse("world_rpg:ability/test/seeded"),
                    CombatSchools.ARCANE.id(),
                    10.0
            );
            first.advance(20);
            second.advance(20);
        }

        assertEquals(first.events(), second.events());
        assertEquals(first.report(), second.report());
    }

    private static HeadlessCombatSimulator simulator(long seed) {
        return new HeadlessCombatSimulator(
                new ProfiledCombatResolver(
                        PROFILE,
                        new SeededCombatRollSource(seed)
                )
        );
    }

    private static CombatActor actor(
            long id,
            double health
    ) {
        CombatActor actor =
                new CombatActor(new CombatActorId(id));
        actor.resources().add(
                HEALTH,
                health,
                health
        );
        return actor;
    }
}
