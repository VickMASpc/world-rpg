package dev.worldrpg.integration.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MinecraftEntityResolver {
    private MinecraftEntityResolver() {
    }

    public static Optional<LivingEntity> findLiving(
            MinecraftServer server,
            UUID uuid
    ) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(uuid, "uuid");

        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof LivingEntity living) {
                return Optional.of(living);
            }
        }

        return Optional.empty();
    }
}
