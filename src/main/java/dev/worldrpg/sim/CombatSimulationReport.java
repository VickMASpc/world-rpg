package dev.worldrpg.sim;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastInterruptionReason;
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
        long interruptions,
        Map<CastInterruptionReason, Long> interruptionsByReason,
        long defeats,
        Map<CombatActorId, Long> defeatsByTarget,
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
                || interruptions < 0
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

        interruptionsByReason = checkedReasonMap(
                interruptionsByReason,
                "interruptionsByReason"
        );

        long summedInterruptions =
                interruptionsByReason.values().stream()
                        .mapToLong(Long::longValue)
                        .sum();

        if (summedInterruptions != interruptions) {
            throw new IllegalArgumentException(
                    "interruptionsByReason total must equal interruptions"
            );
        }

        defeatsByTarget = checkedLongMap(
                defeatsByTarget,
                "defeatsByTarget"
        );

        long summedDefeats = defeatsByTarget.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        if (summedDefeats != defeats) {
            throw new IllegalArgumentException(
                    "defeatsByTarget total must equal defeats"
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

    public long interruptionsOf(
            CastInterruptionReason reason
    ) {
        return interruptionsByReason.getOrDefault(
                Objects.requireNonNull(reason, "reason"),
                0L
        );
    }

    public long defeatsOf(CombatActorId actorId) {
        return defeatsByTarget.getOrDefault(
                Objects.requireNonNull(actorId, "actorId"),
                0L
        );
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

    private static Map<CastInterruptionReason, Long> checkedReasonMap(
            Map<CastInterruptionReason, Long> input,
            String label
    ) {
        Objects.requireNonNull(input, label);

        Map<CastInterruptionReason, Long> copy =
                new LinkedHashMap<>();

        input.forEach((key, value) -> {
            Objects.requireNonNull(key, label + " key");
            long checked = Objects.requireNonNull(
                    value,
                    label + " value"
            );

            if (checked < 0L) {
                throw new IllegalArgumentException(
                        label + " values must be >= 0"
                );
            }

            copy.put(key, checked);
        });

        return java.util.Collections.unmodifiableMap(copy);
    }

    private static Map<CombatActorId, Long> checkedLongMap(
            Map<CombatActorId, Long> input,
            String label
    ) {
        Objects.requireNonNull(input, label);

        Map<CombatActorId, Long> copy =
                new LinkedHashMap<>();

        input.forEach((key, value) -> {
            Objects.requireNonNull(key, label + " key");
            long checked = Objects.requireNonNull(
                    value,
                    label + " value"
            );

            if (checked < 0L) {
                throw new IllegalArgumentException(
                        label + " values must be >= 0"
                );
            }

            copy.put(key, checked);
        });

        return java.util.Collections.unmodifiableMap(copy);
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
