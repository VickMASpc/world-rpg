package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.CastController;

import java.util.Objects;
import java.util.UUID;

public final class ProductionCombatState {
    private final UUID entityUuid;
    private final CombatActor actor;
    private final CastController casts;
    private int level;
    private double maximumHealth;

    public ProductionCombatState(
            UUID entityUuid,
            CombatActor actor,
            CastController casts,
            int level,
            double maximumHealth
    ) {
        this.entityUuid = Objects.requireNonNull(
                entityUuid,
                "entityUuid"
        );
        this.actor = Objects.requireNonNull(actor, "actor");
        this.casts = Objects.requireNonNull(casts, "casts");
        updateCharacter(level, maximumHealth);
    }

    public UUID entityUuid() {
        return entityUuid;
    }

    public CombatActor actor() {
        return actor;
    }

    public CastController casts() {
        return casts;
    }

    public int level() {
        return level;
    }

    public double maximumHealth() {
        return maximumHealth;
    }

    public void updateCharacter(
            int level,
            double maximumHealth
    ) {
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
        this.level = level;
        this.maximumHealth = maximumHealth;
    }
}
