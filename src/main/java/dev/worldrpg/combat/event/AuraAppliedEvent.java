package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.aura.AuraInstanceId;

import java.util.Objects;

public record AuraAppliedEvent(
        long gameTick,
        CombatActorId source,
        CombatActorId target,
        RpgId auraId,
        AuraInstanceId instanceId,
        boolean created,
        int previousStacks,
        int currentStacks
) implements CombatEvent {
    public AuraAppliedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(auraId, "auraId");
        Objects.requireNonNull(instanceId, "instanceId");
    }
}
