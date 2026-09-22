package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.target.TargetObservation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;

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

        LivingEntity sourceLiving = sourceEntity.get();
        LivingEntity targetLiving = targetEntity.get();

        boolean sameWorld =
                sourceLiving.getWorld() == targetLiving.getWorld();

        return TargetObservation.observed(
                sourceLiving == targetLiving,
                sameWorld,
                sourceLiving.isAlive(),
                targetLiving.isAlive(),
                sameWorld && sourceLiving.canSee(targetLiving),
                sameWorld
                        ? OptionalDouble.of(
                                sourceLiving.squaredDistanceTo(targetLiving)
                        )
                        : OptionalDouble.empty()
        );
    }
}
