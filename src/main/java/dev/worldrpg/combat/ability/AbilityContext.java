package dev.worldrpg.combat.ability;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.target.TargetObservation;

import java.util.Objects;

public record AbilityContext(
        CombatActor source,
        CombatActor target,
        long gameTick,
        TargetObservation targetObservation
) {
    public AbilityContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(targetObservation, "targetObservation");

        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }

    public AbilityContext(
            CombatActor source,
            CombatActor target,
            long gameTick
    ) {
        this(
                source,
                target,
                gameTick,
                TargetObservation.unavailable()
        );
    }
}
