package dev.worldrpg.progression;

/**
 * Small inspectable level curve:
 *
 * value(L) = base + linear*x + quadratic*x^2, where x = L - 1.
 *
 * <p>This deliberately avoids hidden exponential compounding.</p>
 */
public record QuadraticLevelCurve(
        int levelCap,
        double levelOneValue,
        double linearPerLevel,
        double quadraticPerLevelSquared
) {
    public QuadraticLevelCurve {
        if (levelCap < 1) {
            throw new IllegalArgumentException(
                    "levelCap must be >= 1"
            );
        }
        requireFinite(levelOneValue, "levelOneValue");
        requireFinite(linearPerLevel, "linearPerLevel");
        requireFinite(
                quadraticPerLevelSquared,
                "quadraticPerLevelSquared"
        );

        for (int level = 1; level <= levelCap; level++) {
            double value = valueUnchecked(level);
            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalArgumentException(
                        "curve must remain finite and >= 0 through level cap"
                );
            }
        }
    }

    public double valueAt(int level) {
        if (level < 1 || level > levelCap) {
            throw new IllegalArgumentException(
                    "level must be between 1 and " + levelCap
            );
        }

        return valueUnchecked(level);
    }

    public double growthMultiple() {
        if (levelOneValue == 0.0) {
            return valueAt(levelCap) == 0.0
                    ? 1.0
                    : Double.POSITIVE_INFINITY;
        }

        return valueAt(levelCap) / levelOneValue;
    }

    private double valueUnchecked(int level) {
        double x = level - 1.0;
        return levelOneValue
                + linearPerLevel * x
                + quadraticPerLevelSquared * x * x;
    }

    private static void requireFinite(
            double value,
            String label
    ) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    label + " must be finite"
            );
        }
    }
}
