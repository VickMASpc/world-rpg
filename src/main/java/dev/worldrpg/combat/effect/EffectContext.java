package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;

public record EffectContext(
        CombatActor source,
        CombatActor target,
        long gameTick
) {
    public EffectContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }
}
