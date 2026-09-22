package dev.worldrpg.combat.ability;

import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;

public record AbilityContext(
        CombatActor source,
        CombatActor target,
        long gameTick
) {
    public AbilityContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }
}
