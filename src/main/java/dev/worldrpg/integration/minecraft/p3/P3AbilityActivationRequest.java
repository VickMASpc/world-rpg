package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;
import java.util.UUID;

/**
 * Minimal client-intent shape.
 *
 * <p>The requester supplies identity only. It never supplies authoritative
 * costs, damage, timing, cooldown or target-legality claims.</p>
 */
public record P3AbilityActivationRequest(
        long sequence,
        RpgId abilityId,
        UUID targetEntityUuid
) {
    public P3AbilityActivationRequest {
        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "request sequence must be >= 0"
            );
        }
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(targetEntityUuid, "targetEntityUuid");
    }
}
