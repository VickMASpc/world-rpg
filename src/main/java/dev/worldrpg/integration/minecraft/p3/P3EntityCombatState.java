package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.CastController;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class P3EntityCombatState {
    private final UUID entityUuid;
    private final CombatActor actor;
    private final CastController casts;
    private Vec3d castStartPosition;

    P3EntityCombatState(
            UUID entityUuid,
            CombatActor actor,
            CastController casts
    ) {
        this.entityUuid = Objects.requireNonNull(entityUuid, "entityUuid");
        this.actor = Objects.requireNonNull(actor, "actor");
        this.casts = Objects.requireNonNull(casts, "casts");
    }

    UUID entityUuid() {
        return entityUuid;
    }

    CombatActor actor() {
        return actor;
    }

    CastController casts() {
        return casts;
    }

    Optional<Vec3d> castStartPosition() {
        return Optional.ofNullable(castStartPosition);
    }

    void markCastStart(Vec3d position) {
        castStartPosition = Objects.requireNonNull(position, "position");
    }

    void clearCastStart() {
        castStartPosition = null;
    }
}
