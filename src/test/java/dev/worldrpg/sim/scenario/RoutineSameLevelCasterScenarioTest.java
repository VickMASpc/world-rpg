package dev.worldrpg.sim.scenario;

import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutineSameLevelCasterScenarioTest {
    @Test
    void seededBatchLivesInsideFirstRoutineFightEnvelope() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new RoutineSameLevelCasterScenario(),
                        1L,
                        256
                );

        double averageSeconds =
                batch.averageElapsedTicks() / 20.0;
        double minimumSeconds =
                batch.minimumElapsedTicks() / 20.0;
        double maximumSeconds =
                batch.maximumElapsedTicks() / 20.0;

        assertTrue(
                averageSeconds >= 20.0
                        && averageSeconds <= 35.0,
                "average routine TTK was " + averageSeconds + " seconds"
        );
        assertTrue(
                minimumSeconds >= 15.0,
                "minimum routine TTK was " + minimumSeconds + " seconds"
        );
        assertTrue(
                maximumSeconds <= 45.0,
                "maximum routine TTK was " + maximumSeconds + " seconds"
        );

        assertEquals(
                1.0,
                batch.averageDefeats(),
                0.0
        );

        double averageMana =
                batch.averageResourceSpent(
                        RoutineSameLevelCasterScenario.MANA
                );

        assertTrue(
                averageMana >= 35.0
                        && averageMana <= 65.0,
                "average mana spend was " + averageMana
        );

        assertTrue(
                batch.damageMissRate() >= 0.02
                        && batch.damageMissRate() <= 0.07,
                "combined miss rate was "
                        + batch.damageMissRate()
        );
    }

    @Test
    void exactSeedIsFullyRepeatable() {
        RoutineSameLevelCasterScenario scenario =
                new RoutineSameLevelCasterScenario();

        assertEquals(
                scenario.run(777L),
                scenario.run(777L)
        );
    }
}
