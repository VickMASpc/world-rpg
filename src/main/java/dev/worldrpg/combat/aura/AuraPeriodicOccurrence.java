package dev.worldrpg.combat.aura;

import java.util.Objects;

public record AuraPeriodicOccurrence(
        AuraInstance instance,
        long scheduledTick
) {
    public AuraPeriodicOccurrence {
        Objects.requireNonNull(instance, "instance");
        if (scheduledTick < 0) {
            throw new IllegalArgumentException("scheduledTick must be >= 0");
        }
    }
}
