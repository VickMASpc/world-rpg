package dev.worldrpg.combat.cast;

import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;

public record CastInterruptionRequest(
        long gameTick,
        CombatActor source,
        CombatActor target,
        CastInterruptionReason reason
) {
    public CastInterruptionRequest {
        if (gameTick < 0) {
            throw new IllegalArgumentException(
                    "gameTick must be >= 0"
            );
        }

        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(reason, "reason");
    }
}
