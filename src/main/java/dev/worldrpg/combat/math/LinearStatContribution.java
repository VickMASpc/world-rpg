package dev.worldrpg.combat.math;

import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

/**
 * One raw-stat contribution into a derived stat.
 */
public record LinearStatContribution(
        StatKey sourceStat,
        double coefficient
) {
    public LinearStatContribution {
        Objects.requireNonNull(sourceStat, "sourceStat");
        if (!Double.isFinite(coefficient)) {
            throw new IllegalArgumentException(
                    "coefficient must be finite"
            );
        }
    }
}
