package dev.worldrpg.combat.math;

/**
 * Positive level-scaled scalar used by P4 calibration profiles.
 */
public record LevelScalarCurve(
        int levelCap,
        double levelOneValue,
        double levelCapValue,
        double growthExponent
) {
    public LevelScalarCurve {
        if (levelCap < 1) {
            throw new IllegalArgumentException(
                    "levelCap must be >= 1"
            );
        }
        requirePositiveFinite(levelOneValue, "levelOneValue");
        requirePositiveFinite(levelCapValue, "levelCapValue");
        requirePositiveFinite(growthExponent, "growthExponent");

        if (levelCapValue < levelOneValue) {
            throw new IllegalArgumentException(
                    "levelCapValue must be >= levelOneValue"
            );
        }
    }

    public static LevelScalarCurve constant(
            int levelCap,
            double value
    ) {
        return new LevelScalarCurve(
                levelCap,
                value,
                value,
                1.0
        );
    }

    public double valueAt(int level) {
        if (level < 1 || level > levelCap) {
            throw new IllegalArgumentException(
                    "level must be between 1 and " + levelCap
            );
        }

        if (levelCap == 1) {
            return levelOneValue;
        }

        double normalized =
                (double) (level - 1)
                        / (double) (levelCap - 1);

        return levelOneValue
                + (levelCapValue - levelOneValue)
                * Math.pow(normalized, growthExponent);
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
