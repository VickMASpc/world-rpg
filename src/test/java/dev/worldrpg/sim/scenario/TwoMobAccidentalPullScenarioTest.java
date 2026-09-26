package dev.worldrpg.sim.scenario;

import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwoMobAccidentalPullScenarioTest {
    @Test
    void simultaneousRoutineEnemiesCreateDangerWithoutBecomingCertainDeath() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new TwoMobAccidentalPullScenario(),
                        1L,
                        256
                );

        double averageSeconds =
                batch.averageElapsedTicks() / 20.0;

        assertTrue(
                averageSeconds >= 50.0
                        && averageSeconds <= 70.0,
                "average double-pull time was "
                        + averageSeconds
                        + " seconds"
        );

        double manaSpent =
                batch.averageResourceSpent(
                        TwoMobAccidentalPullScenario.MANA
                );

        assertTrue(
                manaSpent >= 85.0
                        && manaSpent <= 110.0,
                "average double-pull mana spend was "
                        + manaSpent
        );

        double playerDeathRate =
                batch.defeatRateOf(
                        TwoMobAccidentalPullScenario.PLAYER_ID
                );

        assertTrue(
                playerDeathRate >= 0.01
                        && playerDeathRate <= 0.15,
                "player death rate was "
                        + playerDeathRate
        );

        double averageEnemyDefeats =
                batch.averageDefeatsOf(
                        TwoMobAccidentalPullScenario.FIRST_ENEMY_ID
                )
                + batch.averageDefeatsOf(
                        TwoMobAccidentalPullScenario.SECOND_ENEMY_ID
                );

        assertTrue(
                averageEnemyDefeats >= 1.85
                        && averageEnemyDefeats <= 2.0,
                "average enemy defeats were "
                        + averageEnemyDefeats
        );

        assertEquals(
                0.0,
                batch.averageExplicitRecovery(
                        TwoMobAccidentalPullScenario.HEALTH
                ),
                0.0
        );
        assertEquals(
                0.0,
                batch.averageExplicitRecovery(
                        TwoMobAccidentalPullScenario.MANA
                ),
                0.0
        );
    }

    @Test
    void representativeSeedSurvivesButLeavesPlayerBadlyWounded() {
        RoutineCasterCalibrationSession session =
                new RoutineCasterCalibrationSession(777L);

        double startingHealth = session.playerHealth();

        session.fightTwoRoutineEnemies();

        assertTrue(session.playerHealth() > 0.0);
        assertTrue(
                session.playerHealth()
                        < startingHealth * 0.25,
                "representative bad pull should leave severe health pressure"
        );
        assertTrue(
                session.playerMana()
                        < 80.0,
                "representative bad pull should consume substantial mana"
        );
    }
}
