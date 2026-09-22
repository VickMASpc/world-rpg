package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.aura.AuraInstanceId;
import dev.worldrpg.combat.aura.AuraRemovalReason;

import java.util.Objects;

public record AuraRemovedEvent(
        long gameTick,
        CombatActorId source,
        CombatActorId target,
        RpgId auraId,
        AuraInstanceId instanceId,
        AuraRemovalReason reason
) implements CombatEvent {
    public AuraRemovedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(auraId, "auraId");
        Objects.requireNonNull(instanceId, "instanceId");
        Objects.requireNonNull(reason, "reason");
    }
}
