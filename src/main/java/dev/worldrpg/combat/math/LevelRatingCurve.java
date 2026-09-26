package dev.worldrpg.combat.math;

/**
 * Level-aware cost of one percentage point of a secondary rating.
 *
 * <p>Conversion is deliberately separate from caps, diminishing returns and
 * outcome tables. Those belong to the consumer of the derived stat.</p>
 */
public record LevelRatingCurve(
        int levelCap,
        double ratingPerOnePercentAtLevelOne,
        double ratingPerOnePercentAtLevelCap,
        double growthExponent
) {
    public LevelRatingCurve {
        if (levelCap < 1) {
            throw new IllegalArgumentException(
                    "levelCap must be >= 1"
            );
        }
        requirePositiveFinite(
                ratingPerOnePercentAtLevelOne,
                "ratingPerOnePercentAtLevelOne"
        );
        requirePositiveFinite(
                ratingPerOnePercentAtLevelCap,
                "ratingPerOnePercentAtLevelCap"
        );
        requirePositiveFinite(growthExponent, "growthExponent");

        if (ratingPerOnePercentAtLevelCap
                < ratingPerOnePercentAtLevelOne) {
            throw new IllegalArgumentException(
                    "rating cost must not decrease across the level range"
            );
        }
    }

    public double ratingPerOnePercent(int level) {
        requireLevel(level);

        if (levelCap == 1) {
            return ratingPerOnePercentAtLevelOne;
        }

        double normalized =
                (double) (level - 1)
                        / (double) (levelCap - 1);

        return ratingPerOnePercentAtLevelOne
                + (ratingPerOnePercentAtLevelCap
                - ratingPerOnePercentAtLevelOne)
                * Math.pow(normalized, growthExponent);
    }

    /**
     * Converts raw rating into an additive fractional derived value.
     *
     * <p>Example: 5% is returned as 0.05.</p>
     */
    public double convert(double rating, int level) {
        if (!Double.isFinite(rating)) {
            throw new IllegalArgumentException(
                    "rating must be finite"
            );
        }

        return rating
                / ratingPerOnePercent(level)
                * 0.01;
    }

    private void requireLevel(int level) {
        if (level < 1 || level > levelCap) {
            throw new IllegalArgumentException(
                    "level must be between 1 and " + levelCap
            );
        }
    }

    private static void requirePositiveFinite(
            double value,
            String label
    ) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(
                    label + " must be finite and > 0"
            );
        }
    }
}
