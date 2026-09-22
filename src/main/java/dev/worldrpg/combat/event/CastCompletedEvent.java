package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastId;

import java.util.Objects;

public record CastCompletedEvent(
        long gameTick,
        CastId castId,
        CombatActorId source,
        CombatActorId target,
        RpgId abilityId
) implements CombatEvent {
    public CastCompletedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(castId, "castId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(abilityId, "abilityId");
    }
}
