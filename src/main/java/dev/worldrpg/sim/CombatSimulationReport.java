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
        long defeats,
        double damageApplied,
        double healingApplied,
        double overkill,
        double overheal,
        Map<ResourceKey, Double> resourceSpent,
        Map<ResourceKey, Double> resourceGained,
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
                || criticalResolutions < 0
                || defeats < 0) {
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

        resourceSpent = checkedMap(
                resourceSpent,
                "resourceSpent"
        );
        resourceGained = checkedMap(
                resourceGained,
                "resourceGained"
        );
        explicitRecovery = checkedMap(
                explicitRecovery,
                "explicitRecovery"
        );
    }

    public long elapsedTicks() {
        return endTick - startTick;
    }

    public double spent(ResourceKey resource) {
        return resourceSpent.getOrDefault(
                Objects.requireNonNull(resource, "resource"),
                0.0
        );
    }

    public double gained(ResourceKey resource) {
        return resourceGained.getOrDefault(
                Objects.requireNonNull(resource, "resource"),
                0.0
        );
    }

    private static Map<ResourceKey, Double> checkedMap(
            Map<ResourceKey, Double> input,
            String label
    ) {
        Objects.requireNonNull(input, label);

        Map<ResourceKey, Double> copy =
                new LinkedHashMap<>();

        input.forEach((key, value) -> {
            Objects.requireNonNull(key, label + " key");
            requireNonNegativeFinite(
                    Objects.requireNonNull(
                            value,
                            label + " value"
                    ),
                    label + " value"
            );
            copy.put(key, value);
        });

        return java.util.Collections.unmodifiableMap(copy);
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
