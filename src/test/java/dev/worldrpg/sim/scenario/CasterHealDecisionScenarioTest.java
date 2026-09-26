package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasterHealDecisionScenarioTest {
    @Test
    void interruptingHealPreservesRoutineFightWhileIgnoringItHasRealCost() {
        SimulationBatchReport interrupted =
                SeededSimulationBatchRunner.runSequential(
                        new CasterHealDecisionScenario(
                                CasterHealDecisionScenario.Decision.INTERRUPT
                        ),
                        1L,
                        256
                );

        SimulationBatchReport ignored =
                SeededSimulationBatchRunner.runSequential(
                        new CasterHealDecisionScenario(
                                CasterHealDecisionScenario.Decision.IGNORE
                        ),
                        1L,
                        256
                );

        assertEquals(
                1.0,
                interrupted.averageInterruptionsOf(
                        CastInterruptionReason.INTERRUPT
                ),
                0.0
        );
        assertEquals(
                0.0,
                ignored.averageInterruptionsOf(
                        CastInterruptionReason.INTERRUPT
                ),
                0.0
        );

        assertEquals(
                0.0,
                interrupted.averageHealingApplied(),
                0.0
        );
        assertTrue(
                ignored.averageHealingApplied() >= 55.0,
                "ignored-heal average healing was "
                        + ignored.averageHealingApplied()
        );

        double interruptedSeconds =
                interrupted.averageElapsedTicks() / 20.0;
        double ignoredSeconds =
                ignored.averageElapsedTicks() / 20.0;

        assertTrue(
                interruptedSeconds >= 20.0
                        && interruptedSeconds <= 35.0,
                "interrupted fight averaged "
                        + interruptedSeconds
                        + " seconds"
        );
        assertTrue(
                ignoredSeconds
                        >= interruptedSeconds + 5.0,
                "ignoring heal added only "
                        + (ignoredSeconds - interruptedSeconds)
                        + " seconds"
        );

        double interruptedMana =
                interrupted.averageResourceSpent(
                        CasterHealDecisionScenario.MANA
                );
        double ignoredMana =
                ignored.averageResourceSpent(
                        CasterHealDecisionScenario.MANA
                );

        assertTrue(
                ignoredMana
                        >= interruptedMana + 8.0,
                "ignoring heal added only "
                        + (ignoredMana - interruptedMana)
                        + " mana"
        );

        assertEquals(
                0.0,
                interrupted.defeatRateOf(
                        CasterHealDecisionScenario.PLAYER_ID
                ),
                0.0
        );
        assertEquals(
                0.0,
                ignored.defeatRateOf(
                        CasterHealDecisionScenario.PLAYER_ID
                ),
                0.0
        );
        assertEquals(
                1.0,
                interrupted.defeatRateOf(
                        CasterHealDecisionScenario.ENEMY_ID
                ),
                0.0
        );
        assertEquals(
                1.0,
                ignored.defeatRateOf(
                        CasterHealDecisionScenario.ENEMY_ID
                ),
                0.0
        );
    }
}
