package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

/**
 * Calibrated contact/critical semantics for one resolution-profile identity.
 */
public record CombatOutcomeProfile(
        RpgId id,
        double baseMissChance,
        boolean canCrit
) {
    public CombatOutcomeProfile {
        Objects.requireNonNull(id, "id");

        if (!Double.isFinite(baseMissChance)
                || baseMissChance < 0.0
                || baseMissChance > 1.0) {
            throw new IllegalArgumentException(
                    "baseMissChance must be finite and between 0 and 1"
            );
        }
    }
}
