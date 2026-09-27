package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.event.AbilityActivatedEvent;
import dev.worldrpg.sim.SeededSimulationBatchRunner;
import dev.worldrpg.sim.SimulationBatchReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteGuardianScenarioTest {
    @Test
    void eliteOccupiesLongFightEnvelopeAndConsumesMana() {
        SimulationBatchReport batch =
                SeededSimulationBatchRunner.runSequential(
                        new EliteGuardianScenario(),
                        1L,
                        256
                );

        double seconds =
                batch.averageElapsedTicks() / 20.0;

        assertTrue(
                seconds >= 120.0
                        && seconds <= 300.0,
                "elite average duration was "
                        + seconds
                        + " seconds"
        );

        double manaSpent =
                batch.averageResourceSpent(
                        EliteGuardianScenario.MANA
                );

        assertTrue(
                manaSpent >= 150.0
                        && manaSpent <= 161.0,
                "elite average mana spend was "
                        + manaSpent
        );

        assertTrue(
                batch.averageInterruptionsOf(
                        CastInterruptionReason.INTERRUPT
                ) >= 4.0,
                "elite average interrupts were "
                        + batch.averageInterruptions()
        );

        assertTrue(
                batch.defeatRateOf(
                        EliteGuardianScenario.ELITE_ID
                ) >= 0.85,
                "elite defeat rate was "
                        + batch.defeatRateOf(
                                EliteGuardianScenario.ELITE_ID
                        )
        );

        assertTrue(
                batch.defeatRateOf(
                        EliteGuardianScenario.PLAYER_ID
                ) <= 0.15,
                "player death rate was "
                        + batch.defeatRateOf(
                                EliteGuardianScenario.PLAYER_ID
                        )
        );
    }

    @Test
    void representativeFightActuallyFallsBackAfterManaPressure() {
        EliteGuardianScenario.DetailedResult result =
                new EliteGuardianScenario()
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
                                        EliteGuardianScenario.FALLBACK_ID
                                )
                        )
                        .count();

        assertTrue(
                fallbackActivations > 0L,
                "representative elite fight never used fallback"
        );

        assertTrue(
                result.playerManaRemaining() < 4.0,
                "representative fight should exhaust castable mana"
        );

        assertTrue(
                result.playerHealthRemaining() > 0.0,
                "representative fight should be survivable"
        );
    }
}
