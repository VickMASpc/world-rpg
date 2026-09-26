package dev.worldrpg.sim.scenario;

import dev.worldrpg.progression.WorldRpgMagnitudeDraft;
import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequentialRoutineAttritionScenarioTest {
    @Test
    void twoRoutineFightsCarryResourcePressureWithoutRecovery() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new SequentialRoutineAttritionScenario(),
                        1L,
                        256
                );

        assertEquals(
                2.0,
                batch.averageDefeats(),
                0.0
        );

        assertEquals(
                0.0,
                batch.averageExplicitRecovery(
                        SequentialRoutineAttritionScenario.MANA
                ),
                0.0
        );
        assertEquals(
                0.0,
                batch.averageExplicitRecovery(
                        SequentialRoutineAttritionScenario.HEALTH
                ),
                0.0
        );

        double manaSpent =
                batch.averageResourceSpent(
                        SequentialRoutineAttritionScenario.MANA
                );

        double startingMana =
                WorldRpgMagnitudeDraft
                        .REFERENCE_PRIMARY_RESOURCE
                        .valueAt(
                                RoutineSameLevelCasterScenario.LEVEL
                        );

        assertTrue(
                manaSpent >= 80.0
                        && manaSpent <= 120.0,
                "average two-fight mana spend was "
                        + manaSpent
        );
        assertTrue(
                manaSpent < startingMana,
                "two routine fights must not require a hidden refill"
        );

        double averageSeconds =
                batch.averageElapsedTicks() / 20.0;

        assertTrue(
                averageSeconds >= 45.0
                        && averageSeconds <= 70.0,
                "average two-fight elapsed time was "
                        + averageSeconds
                        + " seconds"
        );
    }

    @Test
    void persistentSessionRetainsVisibleHealthAndManaLoss() {
        RoutineCasterCalibrationSession session =
                new RoutineCasterCalibrationSession(777L);

        double startingHealth = session.playerHealth();
        double startingMana = session.playerMana();

        session.fightRoutineEnemy();

        double afterOneHealth = session.playerHealth();
        double afterOneMana = session.playerMana();

        session.fightRoutineEnemy();

        assertTrue(afterOneHealth < startingHealth);
        assertTrue(afterOneMana < startingMana);
        assertTrue(session.playerHealth() < afterOneHealth);
        assertTrue(session.playerMana() < afterOneMana);
    }
}
