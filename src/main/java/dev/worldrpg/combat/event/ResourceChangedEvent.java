package dev.worldrpg.combat.event;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourceKey;

import java.util.Objects;

public record ResourceChangedEvent(
        long gameTick,
        CombatActorId actorId,
        ResourceKey resource,
        ResourceChange change
) implements CombatEvent {
    public ResourceChangedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(change, "change");
    }
}
