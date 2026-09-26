package dev.worldrpg.combat.math;

import dev.worldrpg.combat.resource.ResourceKey;

import java.util.Objects;

/**
 * Tunable coefficients/caps for one combat-math ruleset.
 *
 * <p>The resolver topology is code. Calibration values live here so simulator
 * work can tune numbers without changing resolution ordering.</p>
 */
public record CombatMathProfile(
        ResourceKey healthResource,
        double physicalPowerCoefficient,
        double spellPowerCoefficient,
        double healingPowerCoefficient,
        double criticalMultiplier,
        double maximumCriticalChance,
        LevelScalarCurve mitigationScale,
        double maximumMitigation
) {
    public CombatMathProfile {
        Objects.requireNonNull(healthResource, "healthResource");
        Objects.requireNonNull(mitigationScale, "mitigationScale");

        requireNonNegativeFinite(
                physicalPowerCoefficient,
                "physicalPowerCoefficient"
        );
        requireNonNegativeFinite(
                spellPowerCoefficient,
                "spellPowerCoefficient"
        );
        requireNonNegativeFinite(
                healingPowerCoefficient,
                "healingPowerCoefficient"
        );

        if (!Double.isFinite(criticalMultiplier)
                || criticalMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "criticalMultiplier must be finite and >= 1"
            );
        }

        requireUnitInterval(
                maximumCriticalChance,
                "maximumCriticalChance"
        );

        requireUnitInterval(
                maximumMitigation,
                "maximumMitigation"
        );
    }

    /**
     * Compatibility/convenience constructor for a flat mitigation scale.
     */
    public CombatMathProfile(
            ResourceKey healthResource,
            double physicalPowerCoefficient,
            double spellPowerCoefficient,
            double healingPowerCoefficient,
            double criticalMultiplier,
            double maximumCriticalChance,
            double mitigationScale,
            double maximumMitigation
    ) {
        this(
                healthResource,
                physicalPowerCoefficient,
                spellPowerCoefficient,
                healingPowerCoefficient,
                criticalMultiplier,
                maximumCriticalChance,
                LevelScalarCurve.constant(
                        100,
                        mitigationScale
                ),
                maximumMitigation
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

    private static void requireUnitInterval(
            double value,
            String label
    ) {
        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {
            throw new IllegalArgumentException(
                    label + " must be finite and between 0 and 1"
            );
        }
    }
}
