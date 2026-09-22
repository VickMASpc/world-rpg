package dev.worldrpg.combat.stat;

import java.util.Objects;

public record StatModifier(
        ModifierHandle handle,
        StatKey stat,
        ModifierSource source,
        StatModifierOperation operation,
        double amount,
        int priority
) {
    public StatModifier {
        Objects.requireNonNull(handle, "handle");
        Objects.requireNonNull(stat, "stat");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(operation, "operation");

        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("modifier amount must be finite");
        }
    }
}
