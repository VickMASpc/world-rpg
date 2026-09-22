package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastId;

import java.util.Objects;

public record CastStartedEvent(
        long gameTick,
        CastId castId,
        CombatActorId source,
        CombatActorId target,
        RpgId abilityId,
        AbilityCastKind castKind,
        long endsAtTick
) implements CombatEvent {
    public CastStartedEvent {
        if (gameTick < 0 || endsAtTick < gameTick) {
            throw new IllegalArgumentException("invalid cast start ticks");
        }
        Objects.requireNonNull(castId, "castId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(castKind, "castKind");
    }
}
