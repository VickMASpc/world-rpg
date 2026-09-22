package dev.worldrpg.sim;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeededSimulationBatchRunnerTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void sequentialBatchUsesExactSeedsAndAggregatesOutcomes() {
        CombatSimulationScenario scenario = seed -> {
            boolean miss = (seed & 1L) == 0L;

            return new CombatSimulationReport(
                    0,
                    100 + seed,
                    1,
                    1,
                    1,
                    miss ? 0 : 1,
                    miss ? 1 : 0,
                    0,
                    0,
                    miss ? 0 : 1,
                    miss ? 1 : 0,
                    miss
                            ? Map.of(CastInterruptionReason.INTERRUPT, 1L)
                            : Map.of(),
                    miss
                            ? Map.of()
                            : Map.of(new CombatActorId(2), 1L),
                    miss ? 0.0 : 10.0,
                    0.0,
                    0.0,
                    0.0,
                    Map.of(MANA, 2.0),
                    Map.of(),
                    Map.of(MANA, 5.0)
            );
        };

        SimulationBatchReport report =
                SeededSimulationBatchRunner.runSequential(
                        scenario,
                        10L,
                        4
                );

        assertEquals(4, report.runCount());
        assertEquals(
                List.of(10L, 11L, 12L, 13L),
                report.runs().stream()
                        .map(SeededSimulationResult::seed)
                        .toList()
        );
        assertEquals(111.5, report.averageElapsedTicks(), 0.0);
        assertEquals(110L, report.minimumElapsedTicks());
        assertEquals(113L, report.maximumElapsedTicks());
        assertEquals(5.0, report.averageDamageApplied(), 0.0);
        assertEquals(0.5, report.damageMissRate(), 0.0);
        assertEquals(0.5, report.averageInterruptions(), 0.0);
        assertEquals(
                0.5,
                report.averageInterruptionsOf(
                        CastInterruptionReason.INTERRUPT
                ),
                0.0
        );
        assertEquals(0.5, report.averageDefeats(), 0.0);
        assertEquals(
                0.5,
                report.defeatRateOf(new CombatActorId(2)),
                0.0
        );
        assertEquals(
                2.0,
                report.averageResourceSpent(MANA),
                0.0
        );
        assertEquals(
                5.0,
                report.averageExplicitRecovery(MANA),
                0.0
        );
    }

    @Test
    void explicitSeedOrderIsPreservedAndRepeatable() {
        CombatSimulationScenario scenario = seed ->
                new CombatSimulationReport(
                        0,
                        Math.floorMod(seed, 100),
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        Map.of(),
                        0,
                        Map.of(),
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        Map.of(),
                        Map.of(),
                        Map.of()
                );

        List<Long> seeds =
                List.of(99L, 4L, -7L, 99L);

        SimulationBatchReport first =
                SeededSimulationBatchRunner.runSeeds(
                        scenario,
                        seeds
                );
        SimulationBatchReport second =
                SeededSimulationBatchRunner.runSeeds(
                        scenario,
                        seeds
                );

        assertEquals(first, second);
        assertEquals(seeds, first.runs().stream()
                .map(SeededSimulationResult::seed)
                .toList());
    }

    @Test
    void emptyBatchIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SeededSimulationBatchRunner.runSeeds(
                        seed -> {
                            throw new AssertionError();
                        },
                        List.of()
                )
        );
    }
}
