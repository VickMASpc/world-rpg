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
    private static final String SPAWN_GROUP_ROOT =
            "authored_mob_spawn_groups";

    private final Map<UUID, RpgId> mobByEntity =
            new LinkedHashMap<>();
    private final Map<UUID, RpgId> spawnGroupByEntity =
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
        spawnGroupByEntity.clear();
        server = null;
    }

    public void bind(
            UUID entityUuid,
            RpgId mobId
    ) {
        bind(entityUuid, mobId, null);
    }

    public void bind(
            UUID entityUuid,
            RpgId mobId,
            RpgId spawnGroupId
    ) {
        requireStarted();
        UUID uuid = Objects.requireNonNull(
                entityUuid,
                "entityUuid"
        );
        mobByEntity.put(
                uuid,
                Objects.requireNonNull(mobId, "mobId")
        );
        if (spawnGroupId == null) {
            spawnGroupByEntity.remove(uuid);
        } else {
            spawnGroupByEntity.put(uuid, spawnGroupId);
        }
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

    public Optional<RpgId> spawnGroupId(UUID entityUuid) {
        requireStarted();
        return Optional.ofNullable(
                spawnGroupByEntity.get(
                        Objects.requireNonNull(
                                entityUuid,
                                "entityUuid"
                        )
                )
        );
    }

    public Map<UUID, RpgId> mobBindings() {
        requireStarted();
        return Map.copyOf(mobByEntity);
    }

    public Map<UUID, RpgId> spawnGroupBindings() {
        requireStarted();
        return Map.copyOf(spawnGroupByEntity);
    }

    public boolean unbind(UUID entityUuid) {
        requireStarted();
        UUID uuid = Objects.requireNonNull(
                entityUuid,
                "entityUuid"
        );
        boolean removed =
                mobByEntity.remove(uuid) != null;
        boolean groupRemoved =
                spawnGroupByEntity.remove(uuid) != null;
        if (removed || groupRemoved) {
            persist();
        }
        return removed;
    }

    public int size() {
        return mobByEntity.size();
    }

    private void load() {
        mobByEntity.clear();
        spawnGroupByEntity.clear();

        NbtCompound worldData =
                WorldRpgPersistentState.get(server)
                        .readWorldData();

        decodeMap(
                worldData,
                ROOT,
                mobByEntity
        );
        decodeMap(
                worldData,
                SPAWN_GROUP_ROOT,
                spawnGroupByEntity
        );
    }

    private static void decodeMap(
            NbtCompound worldData,
            String key,
            Map<UUID, RpgId> output
    ) {
        if (!worldData.contains(
                key,
                NbtElement.COMPOUND_TYPE
        )) {
            return;
        }

        NbtCompound root = worldData.getCompound(key);
        for (String uuidText : root.getKeys()) {
            if (!root.contains(
                    uuidText,
                    NbtElement.STRING_TYPE
            )) {
                continue;
            }

            try {
                output.put(
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

        worldData.put(
                ROOT,
                encodeMap(mobByEntity)
        );
        worldData.put(
                SPAWN_GROUP_ROOT,
                encodeMap(spawnGroupByEntity)
        );
        persistence.writeWorldData(worldData);
    }

    private static NbtCompound encodeMap(
            Map<UUID, RpgId> values
    ) {
        NbtCompound root = new NbtCompound();
        values.forEach((uuid, id) ->
                root.putString(
                        uuid.toString(),
                        id.toString()
                )
        );
        return root;
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "authored mob bindings are not started"
            );
        }
    }
}
