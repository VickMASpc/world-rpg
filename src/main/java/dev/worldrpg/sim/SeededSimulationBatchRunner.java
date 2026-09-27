package dev.worldrpg.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic batch runner.
 *
 * <p>The runner does not own gameplay randomness. It only supplies explicit
 * seeds to a scenario that must construct/use the production formula stack.</p>
 */
public final class SeededSimulationBatchRunner {
    private SeededSimulationBatchRunner() {
    }

    public static SimulationBatchReport runSequential(
            CombatSimulationScenario scenario,
            long firstSeed,
            int runCount
    ) {
        if (runCount < 1) {
            throw new IllegalArgumentException(
                    "runCount must be >= 1"
            );
        }

        List<Long> seeds = new ArrayList<>(runCount);

        for (int i = 0; i < runCount; i++) {
            seeds.add(
                    Math.addExact(firstSeed, (long) i)
            );
        }

        return runSeeds(scenario, seeds);
    }

    public static SimulationBatchReport runSeeds(
            CombatSimulationScenario scenario,
            List<Long> seeds
    ) {
        Objects.requireNonNull(scenario, "scenario");
        Objects.requireNonNull(seeds, "seeds");

        if (seeds.isEmpty()) {
            throw new IllegalArgumentException(
                    "seeds must not be empty"
            );
        }

        List<SeededSimulationResult> results =
                new ArrayList<>(seeds.size());

        for (Long seed : seeds) {
            long checkedSeed =
                    Objects.requireNonNull(seed, "seed");

            CombatSimulationReport report =
                    Objects.requireNonNull(
                            scenario.run(checkedSeed),
                            "scenario report"
                    );

            results.add(
                    new SeededSimulationResult(
                            checkedSeed,
                            report
                    )
            );
        }

        return new SimulationBatchReport(results);
    }
}
