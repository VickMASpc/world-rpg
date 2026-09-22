package dev.worldrpg.combat.stat;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

/**
 * Identifies the runtime source of one or more modifiers.
 *
 * <p>Two instances of the same item/aura definition can therefore be removed
 * independently.</p>
 */
public record ModifierSource(RpgId definitionId, long instanceId) {
    public ModifierSource {
        Objects.requireNonNull(definitionId, "definitionId");
        if (instanceId < 0) {
            throw new IllegalArgumentException("modifier source instanceId must be >= 0");
        }
    }

    public static ModifierSource staticSource(RpgId definitionId) {
        return new ModifierSource(definitionId, 0L);
    }
}
