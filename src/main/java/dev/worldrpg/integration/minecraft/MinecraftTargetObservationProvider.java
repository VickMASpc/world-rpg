package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.target.TargetObservation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Produces fresh target observations from the current Minecraft server state.
 */
public final class MinecraftTargetObservationProvider
        implements AbilityObservationProvider {
    private final MinecraftServer server;
    private final MinecraftCombatActorBindings bindings;

    public MinecraftTargetObservationProvider(
            MinecraftServer server,
            MinecraftCombatActorBindings bindings
    ) {
        this.server = Objects.requireNonNull(server, "server");
        this.bindings = Objects.requireNonNull(bindings, "bindings");
    }

    @Override
    public TargetObservation observe(
            CombatActor source,
            CombatActor target
    ) {
        Optional<UUID> sourceUuid = bindings.entityUuid(source.id());
        Optional<UUID> targetUuid = bindings.entityUuid(target.id());

        if (sourceUuid.isEmpty() || targetUuid.isEmpty()) {
            return TargetObservation.unavailable();
        }

        Optional<LivingEntity> sourceEntity =
                MinecraftEntityResolver.findLiving(server, sourceUuid.get());
        Optional<LivingEntity> targetEntity =
                MinecraftEntityResolver.findLiving(server, targetUuid.get());

        if (sourceEntity.isEmpty() || targetEntity.isEmpty()) {
            return TargetObservation.unavailable();
        }

        return capture(sourceEntity.get(), targetEntity.get());
    }

    public static TargetObservation capture(
            LivingEntity source,
            LivingEntity target
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        boolean sameWorld =
                source.getWorld() == target.getWorld();

        OptionalDouble squaredDistance = sameWorld
                ? OptionalDouble.of(source.squaredDistanceTo(target))
                : OptionalDouble.empty();

        OptionalDouble facingDot = sameWorld
                ? OptionalDouble.of(facingDot(source, target))
                : OptionalDouble.empty();

        return TargetObservation.observed(
                source == target,
                sameWorld,
                source.isAlive(),
                target.isAlive(),
                sameWorld && source.canSee(target),
                squaredDistance,
                facingDot
        );
    }

    private static double facingDot(
            LivingEntity source,
            LivingEntity target
    ) {
        if (source == target) {
            return 1.0;
        }

        Vec3d toTarget = target.getEyePos()
                .subtract(source.getEyePos());

        if (toTarget.lengthSquared() <= 1.0e-12) {
            return 1.0;
        }

        double raw = source.getRotationVec(1.0F)
                .normalize()
                .dotProduct(toTarget.normalize());

        return Math.max(-1.0, Math.min(1.0, raw));
    }
}
