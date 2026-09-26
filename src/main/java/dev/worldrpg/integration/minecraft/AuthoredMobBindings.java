package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AuthoredMobBindings {
    private static final String ROOT = "authored_mob_bindings";

    private final Map<UUID, RpgId> mobByEntity =
            new LinkedHashMap<>();
    private MinecraftServer server;

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(
                server,
                "server"
        );
        load();
    }

    public void stop() {
        mobByEntity.clear();
        server = null;
    }

    public void bind(
            UUID entityUuid,
            RpgId mobId
    ) {
        requireStarted();
        mobByEntity.put(
                Objects.requireNonNull(entityUuid, "entityUuid"),
                Objects.requireNonNull(mobId, "mobId")
        );
        persist();
    }

    public Optional<RpgId> mobId(UUID entityUuid) {
        requireStarted();
        return Optional.ofNullable(
                mobByEntity.get(
                        Objects.requireNonNull(
                                entityUuid,
                                "entityUuid"
                        )
                )
        );
    }

    public boolean unbind(UUID entityUuid) {
        requireStarted();
        boolean removed = mobByEntity.remove(
                Objects.requireNonNull(
                        entityUuid,
                        "entityUuid"
                )
        ) != null;
        if (removed) {
            persist();
        }
        return removed;
    }

    public int size() {
        return mobByEntity.size();
    }

    private void load() {
        mobByEntity.clear();

        NbtCompound worldData =
                WorldRpgPersistentState.get(server)
                        .readWorldData();
        if (!worldData.contains(
                ROOT,
                NbtElement.COMPOUND_TYPE
        )) {
            return;
        }

        NbtCompound root = worldData.getCompound(ROOT);
        for (String uuidText : root.getKeys()) {
            if (!root.contains(
                    uuidText,
                    NbtElement.STRING_TYPE
            )) {
                continue;
            }

            try {
                mobByEntity.put(
                        UUID.fromString(uuidText),
                        RpgId.parse(root.getString(uuidText))
                );
            } catch (IllegalArgumentException ignored) {
                // Stale malformed bindings do not block world load.
            }
        }
    }

    private void persist() {
        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData =
                persistence.readWorldData();

        NbtCompound root = new NbtCompound();
        mobByEntity.forEach((uuid, mobId) ->
                root.putString(
                        uuid.toString(),
                        mobId.toString()
                )
        );

        worldData.put(ROOT, root);
        persistence.writeWorldData(worldData);
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "authored mob bindings are not started"
            );
        }
    }
}
