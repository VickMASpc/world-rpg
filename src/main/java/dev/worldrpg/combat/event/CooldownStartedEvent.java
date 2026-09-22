package dev.worldrpg.combat.event;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cooldown.CooldownKey;

import java.util.Objects;

public record CooldownStartedEvent(
        long gameTick,
        CombatActorId actorId,
        CooldownKey cooldown,
        long readyAtTick
) implements CombatEvent {
    public CooldownStartedEvent {
        if (gameTick < 0 || readyAtTick < gameTick) {
            throw new IllegalArgumentException("invalid cooldown event ticks");
        }
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(cooldown, "cooldown");
    }
}
