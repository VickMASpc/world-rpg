package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.integration.minecraft.MinecraftEntityResolver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Disposable in-world P3 integration harness.
 *
 * <p>It deliberately spawns a real Minecraft hostile entity while keeping
 * combat outcomes in the RPG runtime. This is not game content.</p>
 */
public final class P3DeveloperRoom {
    private final P3DeveloperCombatRuntime combat;
    private final Map<UUID, UUID> targetByPlayer = new LinkedHashMap<>();

    private MinecraftServer server;

    public P3DeveloperRoom(P3DeveloperCombatRuntime combat) {
        this.combat = Objects.requireNonNull(combat, "combat");
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    public void stop() {
        removeAllTargets();
        targetByPlayer.clear();
        server = null;
    }

    public HuskEntity spawnTarget(
            ServerPlayerEntity player,
            int distance
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        validateDistance(distance);

        removeTarget(player);

        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos position = player.getBlockPos()
                .offset(player.getHorizontalFacing(), distance);

        HuskEntity target = EntityType.HUSK.spawn(
                world,
                position,
                SpawnReason.COMMAND
        );

        if (target == null) {
            throw new IllegalStateException(
                    "Failed to spawn P3 developer target at " + position
            );
        }

        target.setAiDisabled(true);
        target.setPersistent();
        target.setInvulnerable(true);
        target.setCustomName(Text.literal("P3 RPG Target"));
        target.setCustomNameVisible(true);

        targetByPlayer.put(player.getUuid(), target.getUuid());

        // Bind/initialize immediately so status is stable before the first cast.
        combat.status(target);
        combat.status(player);

        return target;
    }

    /**
     * Relocates the existing target without replacing its entity UUID or RPG
     * state. This exists specifically for cast-resolution range revalidation.
     */
    public LivingEntity moveTarget(
            ServerPlayerEntity player,
            int distance
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        validateDistance(distance);

        LivingEntity target = requireTarget(player);
        BlockPos position = player.getBlockPos()
                .offset(player.getHorizontalFacing(), distance);

        target.refreshPositionAndAngles(
                position,
                target.getYaw(),
                target.getPitch()
        );
        return target;
    }

    /**
     * Direct developer-gate setup. It changes only the target's proof-health
     * resource and does not touch the Minecraft entity's vanilla health.
     */
    public double setTargetProofHealth(
            ServerPlayerEntity player,
            double value
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        return combat.setProofHealth(requireTarget(player), value);
    }

    public Optional<UUID> targetUuid(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");
        return Optional.ofNullable(targetByPlayer.get(player.getUuid()));
    }

    public Optional<String> status(ServerPlayerEntity player) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        UUID targetUuid = targetByPlayer.get(player.getUuid());
        if (targetUuid == null) {
            return Optional.empty();
        }

        return MinecraftEntityResolver.findLiving(server, targetUuid)
                .map(target -> "player[" + combat.status(player)
                        + "] target[" + combat.status(target) + "]");
    }

    public boolean removeTarget(ServerPlayerEntity player) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        UUID targetUuid = targetByPlayer.remove(player.getUuid());
        if (targetUuid == null) {
            return false;
        }

        combat.removeState(targetUuid);

        MinecraftEntityResolver.findLiving(server, targetUuid)
                .ifPresent(target -> target.discard());

        return true;
    }

    public void reset(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");
        removeTarget(player);
        combat.removeState(player.getUuid());
    }

    private LivingEntity requireTarget(ServerPlayerEntity player) {
        UUID targetUuid = targetByPlayer.get(player.getUuid());
        if (targetUuid == null) {
            throw new IllegalStateException(
                    "No P3 developer target is active."
            );
        }

        return MinecraftEntityResolver.findLiving(server, targetUuid)
                .orElseThrow(() -> new IllegalStateException(
                        "P3 developer target entity is no longer available."
                ));
    }

    private static void validateDistance(int distance) {
        if (distance < 1 || distance > 30) {
            throw new IllegalArgumentException(
                    "developer-room distance must be between 1 and 30 blocks"
            );
        }
    }

    private void removeAllTargets() {
        if (server == null) {
            return;
        }

        for (UUID targetUuid : List.copyOf(targetByPlayer.values())) {
            combat.removeState(targetUuid);
            MinecraftEntityResolver.findLiving(server, targetUuid)
                    .ifPresent(target -> target.discard());
        }
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "P3 developer room is not started"
            );
        }
    }
}
