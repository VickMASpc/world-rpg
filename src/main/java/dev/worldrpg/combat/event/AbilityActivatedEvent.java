package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;

import java.util.Objects;

public record AbilityActivatedEvent(
        long gameTick,
        CombatActorId source,
        CombatActorId target,
        RpgId abilityId
) implements CombatEvent {
    public AbilityActivatedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(abilityId, "abilityId");
    }
}
