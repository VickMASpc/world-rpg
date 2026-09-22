package dev.worldrpg.combat.math;

/**
 * Reference P4 defense calibration.
 *
 * <p>These values are simulator targets, not final class/gear budgets.</p>
 */
public final class WorldRpgDefenseDraft {
    public static final int LEVEL_CAP = 100;

    /**
     * Scaling denominator used by damage mitigation.
     *
     * <p>It rises roughly with the long-horizon health scale without creating
     * an exponential stat wall.</p>
     */
    public static final LevelScalarCurve MITIGATION_SCALE =
            new LevelScalarCurve(
                    LEVEL_CAP,
                    100.0,
                    900.0,
                    0.80
            );

    public static final double REFERENCE_ARMOR_MITIGATION =
            0.25;

    public static final double REFERENCE_RESISTANCE_MITIGATION =
            0.15;

    private WorldRpgDefenseDraft() {
    }

    public static double referenceArmor(int level) {
        return defenseForMitigation(
                MITIGATION_SCALE.valueAt(level),
                REFERENCE_ARMOR_MITIGATION
        );
    }

    public static double referenceResistance(int level) {
        return defenseForMitigation(
                MITIGATION_SCALE.valueAt(level),
                REFERENCE_RESISTANCE_MITIGATION
        );
    }

    public static double defenseForMitigation(
            double scale,
            double mitigation
    ) {
        if (!Double.isFinite(scale) || scale <= 0.0) {
            throw new IllegalArgumentException(
                    "scale must be finite and > 0"
            );
        }
        if (!Double.isFinite(mitigation)
                || mitigation < 0.0
                || mitigation >= 1.0) {
            throw new IllegalArgumentException(
                    "mitigation must be finite and in [0, 1)"
            );
        }

        if (mitigation == 0.0) {
            return 0.0;
        }

        return scale * mitigation / (1.0 - mitigation);
    }

    public static double mitigation(
            double defense,
            double scale
    ) {
        if (!Double.isFinite(defense) || defense < 0.0) {
            throw new IllegalArgumentException(
                    "defense must be finite and >= 0"
            );
        }
        if (!Double.isFinite(scale) || scale <= 0.0) {
            throw new IllegalArgumentException(
                    "scale must be finite and > 0"
            );
        }

        return defense == 0.0
                ? 0.0
                : defense / (defense + scale);
    }
}
