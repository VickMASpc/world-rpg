package dev.worldrpg.combat.aura;

import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;

import java.util.Objects;

/**
 * Stat contribution per aura stack.
 */
public record AuraStatModifier(
        StatKey stat,
        StatModifierOperation operation,
        double amountPerStack,
        int priority
) {
    public AuraStatModifier {
        Objects.requireNonNull(stat, "stat");
        Objects.requireNonNull(operation, "operation");

        if (!Double.isFinite(amountPerStack)) {
            throw new IllegalArgumentException("aura stat amount must be finite");
        }
    }
}
