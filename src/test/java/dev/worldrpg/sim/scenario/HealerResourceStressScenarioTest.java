package dev.worldrpg.sim.scenario;

import dev.worldrpg.progression.WorldRpgMagnitudeDraft;
import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HealerResourceStressScenarioTest {
    @Test
    void sustainedPressureNearlyExhaustsHealerWithoutBecomingCertainWipe() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new HealerResourceStressScenario(),
                        1L,
                        256
                );

        double seconds =
                batch.averageElapsedTicks() / 20.0;

        assertTrue(
                seconds >= 240.0
                        && seconds <= 250.0,
                "average pressure duration was "
                        + seconds
                        + " seconds"
        );

        double manaSpent =
                batch.averageResourceSpent(
                        HealerResourceStressScenario.MANA
                );

        assertTrue(
                manaSpent >= 155.0
                        && manaSpent <= 161.0,
                "average healer mana spend was "
                        + manaSpent
        );

        assertTrue(
                batch.averageHealingApplied() >= 550.0,
                "average healing applied was "
                        + batch.averageHealingApplied()
        );

        double allyDeathRate =
                batch.defeatRateOf(
                        HealerResourceStressScenario.ALLY_ID
                );

        assertTrue(
                allyDeathRate >= 0.01
                        && allyDeathRate <= 0.15,
                "ally death rate was "
                        + allyDeathRate
        );

        assertTrue(
                batch.averageExplicitRecovery(
                        HealerResourceStressScenario.MANA
                ) == 0.0
        );
    }

    @Test
    void representativeRunEndsLowOnManaButStillAlive() {
        HealerResourceStressScenario.DetailedResult result =
                new HealerResourceStressScenario()
                        .runDetailed(777L);

        assertTrue(
                result.healerManaRemaining() < 1.0,
                "representative healer should be effectively out of mana"
        );
        assertTrue(
                result.allyHealthRemaining() > 0.0,
                "representative ally should survive"
        );
        assertTrue(
                result.allyHealthRemaining()
                        < WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                                .valueAt(
                                        HealerResourceStressScenario.LEVEL
                                ),
                "representative ally should finish under meaningful pressure"
        );
    }
}
