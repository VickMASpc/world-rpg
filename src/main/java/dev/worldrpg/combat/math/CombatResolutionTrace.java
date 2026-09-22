package dev.worldrpg.combat.math;

import java.util.OptionalDouble;

/**
 * Numerical trace for one resolved magnitude.
 *
 * <p>Values are intentionally retained without presentation rounding so
 * simulator regressions can explain every stage.</p>
 */
public record CombatResolutionTrace(
        double authoredBase,
        double powerContribution,
        double afterScaling,
        double outgoingMultiplier,
        double afterOutgoing,
        double criticalChance,
        OptionalDouble criticalRoll,
        boolean critical,
        double criticalMultiplierApplied,
        double afterCritical,
        double mitigationFraction,
        double afterMitigation,
        double incomingMultiplier,
        double requestedFinal,
        double appliedFinal,
        double excess
) {
    public CombatResolutionTrace {
        java.util.Objects.requireNonNull(
                criticalRoll,
                "criticalRoll"
        );

        requireNonNegativeFinite(authoredBase, "authoredBase");
        requireFinite(powerContribution, "powerContribution");
        requireNonNegativeFinite(afterScaling, "afterScaling");
        requireNonNegativeFinite(outgoingMultiplier, "outgoingMultiplier");
        requireNonNegativeFinite(afterOutgoing, "afterOutgoing");
        requireUnitInterval(criticalChance, "criticalChance");
        if (criticalRoll.isPresent()) {
            double roll = criticalRoll.getAsDouble();
            if (!Double.isFinite(roll) || roll < 0.0 || roll >= 1.0) {
                throw new IllegalArgumentException(
                        "criticalRoll must be in [0, 1)"
                );
            }
        }
        if (!Double.isFinite(criticalMultiplierApplied)
                || criticalMultiplierApplied < 1.0) {
            throw new IllegalArgumentException(
                    "criticalMultiplierApplied must be finite and >= 1"
            );
        }
        requireNonNegativeFinite(afterCritical, "afterCritical");
        requireUnitInterval(mitigationFraction, "mitigationFraction");
        requireNonNegativeFinite(afterMitigation, "afterMitigation");
        requireNonNegativeFinite(incomingMultiplier, "incomingMultiplier");
        requireNonNegativeFinite(requestedFinal, "requestedFinal");
        requireNonNegativeFinite(appliedFinal, "appliedFinal");
        requireNonNegativeFinite(excess, "excess");

        if (appliedFinal > requestedFinal + 1.0e-12) {
            throw new IllegalArgumentException(
                    "appliedFinal cannot exceed requestedFinal"
            );
        }
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

    private static void requireUnitInterval(
            double value,
            String label
    ) {
        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {
            throw new IllegalArgumentException(
                    label + " must be between 0 and 1"
            );
        }
    }
}
