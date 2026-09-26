package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.CastController;

import java.util.Objects;
import java.util.UUID;

public record ProductionCombatState(
        UUID entityUuid,
        CombatActor actor,
        CastController casts,
        int level,
        double maximumHealth
) {
    public ProductionCombatState {
        Objects.requireNonNull(entityUuid, "entityUuid");
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(casts, "casts");
        if (level < 1) {
            throw new IllegalArgumentException(
                    "level must be >= 1"
            );
        }
        if (!Double.isFinite(maximumHealth)
                || maximumHealth <= 0.0) {
            throw new IllegalArgumentException(
                    "maximumHealth must be finite and > 0"
            );
        }
    }
}
