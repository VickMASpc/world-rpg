package dev.worldrpg.combat.resolution;

import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

/**
 * One explicit stat contribution to a resolved combat magnitude.
 */
public record CombatPowerTerm(
        StatKey stat,
        double coefficient
) {
    public CombatPowerTerm {
        Objects.requireNonNull(stat, "stat");

        if (!Double.isFinite(coefficient)) {
            throw new IllegalArgumentException(
                    "coefficient must be finite"
            );
        }
    }
}
