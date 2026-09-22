package dev.worldrpg.combat.cast;

import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;
import java.util.OptionalLong;

public final class ActiveCast {
    private final CastId id;
    private final AbilityDefinition ability;
    private final CombatActor source;
    private final CombatActor target;
    private final long startedAtTick;
    private final long endsAtTick;
    private OptionalLong nextChannelTick;

    ActiveCast(
            CastId id,
            AbilityDefinition ability,
            CombatActor source,
            CombatActor target,
            long startedAtTick,
            long endsAtTick,
            OptionalLong nextChannelTick
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.ability = Objects.requireNonNull(ability, "ability");
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
        this.startedAtTick = startedAtTick;
        this.endsAtTick = endsAtTick;
        this.nextChannelTick = Objects.requireNonNull(nextChannelTick, "nextChannelTick");
    }

    public CastId id() {
        return id;
    }

    public AbilityDefinition ability() {
        return ability;
    }

    public CombatActor source() {
        return source;
    }

    public CombatActor target() {
        return target;
    }

    public long startedAtTick() {
        return startedAtTick;
    }

    public long endsAtTick() {
        return endsAtTick;
    }

    public OptionalLong nextChannelTick() {
        return nextChannelTick;
    }

    void setNextChannelTick(OptionalLong nextChannelTick) {
        this.nextChannelTick = Objects.requireNonNull(nextChannelTick, "nextChannelTick");
    }
}
