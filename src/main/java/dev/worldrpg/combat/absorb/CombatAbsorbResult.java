package dev.worldrpg.combat.absorb;

/**
 * One aggregate absorb resolution.
 */
public record CombatAbsorbResult(
        double incoming,
        double absorbed,
        double remaining
) {
    public CombatAbsorbResult {
        requireNonNegativeFinite(incoming, "incoming");
        requireNonNegativeFinite(absorbed, "absorbed");
        requireNonNegativeFinite(remaining, "remaining");

        double tolerance =
                1.0e-9 * Math.max(1.0, incoming);

        if (Math.abs(
                incoming - (absorbed + remaining)
        ) > tolerance) {
            throw new IllegalArgumentException(
                    "absorbed + remaining must equal incoming"
            );
        }
    }

    public static CombatAbsorbResult none(double incoming) {
        return new CombatAbsorbResult(
                incoming,
                0.0,
                incoming
        );
    }

    private static void requireNonNegativeFinite(
            double value,
            String label
    ) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    label + " must be finite and >= 0"
            );
        }
    }
}
