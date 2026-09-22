package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;

import java.util.Objects;

/**
 * Semantic transition emitted once when resolved damage moves health from a
 * positive value to zero.
 */
public record CombatActorDefeatedEvent(
        long gameTick,
        CombatActorId sourceActorId,
        CombatActorId targetActorId,
        RpgId causeId
) implements CombatEvent {
    public CombatActorDefeatedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException(
                    "gameTick must be >= 0"
            );
        }

        Objects.requireNonNull(sourceActorId, "sourceActorId");
        Objects.requireNonNull(targetActorId, "targetActorId");
        Objects.requireNonNull(causeId, "causeId");
    }
}
