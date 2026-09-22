package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastId;
import dev.worldrpg.combat.cast.CastInterruptionReason;

import java.util.Objects;

public record CastInterruptedEvent(
        long gameTick,
        CastId castId,
        CombatActorId source,
        RpgId abilityId,
        CastInterruptionReason reason
) implements CombatEvent {
    public CastInterruptedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(castId, "castId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(reason, "reason");
    }
}
