package dev.worldrpg.combat.math;

import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

/**
 * Converts one raw secondary-rating stat into an additive derived fraction.
 */
public record RatingConversionRule(
        StatKey ratingStat,
        StatKey targetStat,
        LevelRatingCurve curve
) {
    public RatingConversionRule {
        Objects.requireNonNull(ratingStat, "ratingStat");
        Objects.requireNonNull(targetStat, "targetStat");
        Objects.requireNonNull(curve, "curve");
    }
}
