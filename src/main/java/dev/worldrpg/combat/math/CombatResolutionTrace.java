package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Numerical trace for one resolved magnitude.
 *
 * <p>Values are intentionally retained without presentation rounding so
 * simulator regressions can explain every stage.</p>
 */
public record CombatResolutionTrace(
        RpgId resolutionProfileId,
        CombatContactOutcome contactOutcome,
        double missChance,
        OptionalDouble contactRoll,
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
        double beforeAbsorb,
        double absorbed,
        double requestedFinal,
        double appliedFinal,
        double excess
) {
    public CombatResolutionTrace {
        Objects.requireNonNull(
                resolutionProfileId,
                "resolutionProfileId"
        );
        Objects.requireNonNull(
                contactOutcome,
                "contactOutcome"
        );
        Objects.requireNonNull(contactRoll, "contactRoll");
        Objects.requireNonNull(criticalRoll, "criticalRoll");

        requireUnitInterval(missChance, "missChance");
        requireRoll(contactRoll, "contactRoll");

        requireNonNegativeFinite(authoredBase, "authoredBase");
        requireFinite(powerContribution, "powerContribution");
        requireNonNegativeFinite(afterScaling, "afterScaling");
        requireNonNegativeFinite(outgoingMultiplier, "outgoingMultiplier");
        requireNonNegativeFinite(afterOutgoing, "afterOutgoing");
        requireUnitInterval(criticalChance, "criticalChance");
        requireRoll(criticalRoll, "criticalRoll");

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
        requireNonNegativeFinite(beforeAbsorb, "beforeAbsorb");
        requireNonNegativeFinite(absorbed, "absorbed");
        requireNonNegativeFinite(requestedFinal, "requestedFinal");

        double absorbTolerance =
                1.0e-9 * Math.max(1.0, beforeAbsorb);
        if (Math.abs(
                beforeAbsorb - (absorbed + requestedFinal)
        ) > absorbTolerance) {
            throw new IllegalArgumentException(
                    "absorbed + requestedFinal must equal beforeAbsorb"
            );
        }
        requireNonNegativeFinite(appliedFinal, "appliedFinal");
        requireNonNegativeFinite(excess, "excess");

        if (appliedFinal > requestedFinal + 1.0e-12) {
            throw new IllegalArgumentException(
                    "appliedFinal cannot exceed requestedFinal"
            );
        }

        if (contactOutcome == CombatContactOutcome.MISS) {
            if (critical
                    || beforeAbsorb != 0.0
                    || absorbed != 0.0
                    || requestedFinal != 0.0
                    || appliedFinal != 0.0
                    || excess != 0.0) {
                throw new IllegalArgumentException(
                        "missed magnitude cannot crit or apply resource change"
                );
            }
        }
    }

    private static void requireRoll(
            OptionalDouble roll,
            String label
    ) {
        if (roll.isEmpty()) {
            return;
        }

        double value = roll.getAsDouble();

        if (!Double.isFinite(value)
                || value < 0.0
                || value >= 1.0) {
            throw new IllegalArgumentException(
                    label + " must be in [0, 1)"
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
