package dev.worldrpg.sim;

import dev.worldrpg.combat.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record CombatSimulationReport(
        long startTick,
        long endTick,
        int eventCount,
        long resolutions,
        long damageResolutions,
        long damageHits,
        long damageMisses,
        long healingResolutions,
        long criticalResolutions,
        double damageApplied,
        double healingApplied,
        double overkill,
        double overheal,
        Map<ResourceKey, Double> explicitRecovery
) {
    public CombatSimulationReport {
        if (startTick < 0 || endTick < startTick) {
            throw new IllegalArgumentException(
                    "invalid simulation tick range"
            );
        }
        if (eventCount < 0
                || resolutions < 0
                || damageResolutions < 0
                || damageHits < 0
                || damageMisses < 0
                || healingResolutions < 0
                || criticalResolutions < 0) {
            throw new IllegalArgumentException(
                    "simulation counters must be >= 0"
            );
        }

        if (damageHits + damageMisses != damageResolutions) {
            throw new IllegalArgumentException(
                    "damageHits + damageMisses must equal damageResolutions"
            );
        }

        requireNonNegativeFinite(damageApplied, "damageApplied");
        requireNonNegativeFinite(healingApplied, "healingApplied");
        requireNonNegativeFinite(overkill, "overkill");
        requireNonNegativeFinite(overheal, "overheal");

        explicitRecovery = java.util.Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Objects.requireNonNull(
                                explicitRecovery,
                                "explicitRecovery"
                        )
                )
        );
    }

    public long elapsedTicks() {
        return endTick - startTick;
    }

    private static void requireNonNegativeFinite(
            double value,
            String label
    ) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    label + " must be finite and >= 0"
            );
        }
    }
}
