package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.event.AbilityActivatedEvent;
import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonTrashGroupScenarioTest {
    @Test
    void dungeonPackLivesInsideGroupCombatEnvelope() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new DungeonTrashGroupScenario(),
                        1L,
                        256
                );

        double seconds =
                batch.averageElapsedTicks() / 20.0;

        assertTrue(
                seconds >= 90.0
                        && seconds <= 150.0,
                "dungeon pack averaged "
                        + seconds
                        + " seconds"
        );

        assertTrue(
                batch.defeatRateOf(
                        DungeonTrashGroupScenario.GUARD_ID
                ) >= 0.98
        );
        assertTrue(
                batch.defeatRateOf(
                        DungeonTrashGroupScenario.SKIRMISHER_ID
                ) >= 0.98
        );
        assertTrue(
                batch.defeatRateOf(
                        DungeonTrashGroupScenario.CASTER_ID
                ) >= 0.98
        );

        assertTrue(
                batch.defeatRateOf(
                        DungeonTrashGroupScenario.TANK_ID
                ) <= 0.05,
                "tank death rate was "
                        + batch.defeatRateOf(
                                DungeonTrashGroupScenario.TANK_ID
                        )
        );

        assertTrue(
                batch.averageInterruptionsOf(
                        CastInterruptionReason.INTERRUPT
                ) >= 1.0,
                "average interrupts were "
                        + batch.averageInterruptions()
        );

        double healerMana =
                batch.averageResourceSpent(
                        DungeonTrashGroupScenario.MANA
                );

        assertTrue(
                healerMana >= 190.0
                        && healerMana <= 260.0,
                "combined party mana spend was "
                        + healerMana
        );

        assertTrue(
                batch.averageExplicitRecovery(
                        DungeonTrashGroupScenario.MANA
                ) == 0.0
        );
    }

    @Test
    void representativePackUsesFallbackAndClearsAllEnemies() {
        DungeonTrashGroupScenario.DetailedResult result =
                new DungeonTrashGroupScenario()
                        .runDetailed(777L);

        long fallbackActivations =
                result.events().stream()
                        .filter(event ->
                                event instanceof AbilityActivatedEvent
                        )
                        .map(event ->
                                (AbilityActivatedEvent) event
                        )
                        .filter(event ->
                                event.abilityId().equals(
                                        DungeonTrashGroupScenario.DPS_FALLBACK_ID
                                )
                        )
                        .count();

        assertTrue(
                fallbackActivations > 0L,
                "representative dungeon pack never used DPS fallback"
        );

        assertTrue(
                result.dpsManaRemaining() < 4.0,
                "representative DPS should exhaust castable mana"
        );
        assertTrue(
                result.healerManaRemaining() > 50.0,
                "healer should retain reserve in this trash pack"
        );
        assertTrue(
                result.tankHealthRemaining() > 0.0,
                "representative tank should survive"
        );

        assertTrue(
                result.report().defeatsOf(
                        DungeonTrashGroupScenario.GUARD_ID
                ) == 1L
        );
        assertTrue(
                result.report().defeatsOf(
                        DungeonTrashGroupScenario.SKIRMISHER_ID
                ) == 1L
        );
        assertTrue(
                result.report().defeatsOf(
                        DungeonTrashGroupScenario.CASTER_ID
                ) == 1L
        );
    }
}
