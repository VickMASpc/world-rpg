package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;

import java.util.Objects;

/**
 * Aggregate semantic fact for damage prevented by absorb mechanics.
 */
public record CombatAbsorbedEvent(
        long gameTick,
        CombatActorId sourceActorId,
        CombatActorId targetActorId,
        RpgId causeId,
        RpgId schoolId,
        double amount
) implements CombatEvent {
    public CombatAbsorbedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException(
                    "gameTick must be >= 0"
            );
        }

        Objects.requireNonNull(sourceActorId, "sourceActorId");
        Objects.requireNonNull(targetActorId, "targetActorId");
        Objects.requireNonNull(causeId, "causeId");
        Objects.requireNonNull(schoolId, "schoolId");

        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException(
                    "amount must be finite and > 0"
            );
        }
    }
}
