package dev.worldrpg.combat.aura;

import dev.worldrpg.combat.actor.CombatActorId;

import java.util.Objects;
import java.util.OptionalLong;

public final class AuraInstance {
    private final AuraInstanceId id;
    private final AuraDefinition definition;
    private final CombatActorId source;
    private final CombatActorId target;
    private final long appliedAtTick;
    private int stacks;
    private OptionalLong expiresAtTick;

    AuraInstance(
            AuraInstanceId id,
            AuraDefinition definition,
            CombatActorId source,
            CombatActorId target,
            long appliedAtTick,
            int stacks,
            OptionalLong expiresAtTick
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.definition = Objects.requireNonNull(definition, "definition");
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
        this.appliedAtTick = appliedAtTick;
        this.stacks = stacks;
        this.expiresAtTick = Objects.requireNonNull(expiresAtTick, "expiresAtTick");
    }

    public AuraInstanceId id() {
        return id;
    }

    public AuraDefinition definition() {
        return definition;
    }

    public CombatActorId source() {
        return source;
    }

    public CombatActorId target() {
        return target;
    }

    public long appliedAtTick() {
        return appliedAtTick;
    }

    public int stacks() {
        return stacks;
    }

    public OptionalLong expiresAtTick() {
        return expiresAtTick;
    }

    void setStacks(int stacks) {
        this.stacks = stacks;
    }

    void setExpiresAtTick(OptionalLong expiresAtTick) {
        this.expiresAtTick = Objects.requireNonNull(expiresAtTick, "expiresAtTick");
    }
}
