package dev.worldrpg.sim;

import java.util.Objects;

public record SeededSimulationResult(
        long seed,
        CombatSimulationReport report
) {
    public SeededSimulationResult {
        Objects.requireNonNull(report, "report");
    }
}
