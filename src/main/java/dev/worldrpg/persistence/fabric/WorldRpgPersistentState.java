package dev.worldrpg.persistence.fabric;

import dev.worldrpg.persistence.SaveSchemaVersion;
import dev.worldrpg.persistence.WorldRpgSaveSchema;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class WorldRpgPersistentState extends PersistentState {
    private static final String STORAGE_ID = "world_rpg";
    private static final String SCHEMA = "schema";
    private static final String WORLD = "world";
    private static final String PLAYERS = "players";

    private static final Type<WorldRpgPersistentState> TYPE =
            new Type<>(
                    WorldRpgPersistentState::new,
                    WorldRpgPersistentState::fromNbt,
                    null
            );

    private NbtCompound worldData = new NbtCompound();
    private final Map<UUID, NbtCompound> playerData =
            new LinkedHashMap<>();

    public WorldRpgPersistentState() {
    }

    public static WorldRpgPersistentState get(
            MinecraftServer server
    ) {
        Objects.requireNonNull(server, "server");

        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException(
                    "Minecraft server has no overworld for World RPG persistence"
            );
        }

        return overworld.getPersistentStateManager()
                .getOrCreate(TYPE, STORAGE_ID);
    }

    public static WorldRpgPersistentState fromNbt(
            NbtCompound input,
            RegistryWrapper.WrapperLookup registryLookup
    ) {
        Objects.requireNonNull(input, "input");

        int schemaValue = input.contains(SCHEMA, NbtElement.INT_TYPE)
                ? input.getInt(SCHEMA)
                : 1;

        var migrated = WorldRpgSaveSchema.MIGRATOR.migrate(
                input.copy(),
                new SaveSchemaVersion(schemaValue)
        );

        NbtCompound root = migrated.value();
        WorldRpgPersistentState state =
                new WorldRpgPersistentState();

        if (root.contains(WORLD, NbtElement.COMPOUND_TYPE)) {
            state.worldData = root.getCompound(WORLD).copy();
        }

        if (root.contains(PLAYERS, NbtElement.COMPOUND_TYPE)) {
            NbtCompound players = root.getCompound(PLAYERS);

            for (String key : players.getKeys()) {
                if (!players.contains(key, NbtElement.COMPOUND_TYPE)) {
                    throw new IllegalStateException(
                            "player persistence entry is not a compound: " + key
                    );
                }

                final UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalStateException(
                            "invalid persisted player UUID: " + key,
                            exception
                    );
                }

                state.playerData.put(
                        uuid,
                        players.getCompound(key).copy()
                );
            }
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registryLookup
    ) {
        Objects.requireNonNull(nbt, "nbt");

        nbt.putInt(SCHEMA, WorldRpgSaveSchema.CURRENT.value());
        nbt.put(WORLD, worldData.copy());

        NbtCompound players = new NbtCompound();
        playerData.forEach((uuid, data) ->
                players.put(uuid.toString(), data.copy())
        );
        nbt.put(PLAYERS, players);

        return nbt;
    }

    public NbtCompound readWorldData() {
        return worldData.copy();
    }

    public void writeWorldData(NbtCompound data) {
        worldData = Objects.requireNonNull(data, "data").copy();
        markDirty();
    }

    public NbtCompound readPlayerData(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");

        NbtCompound data = playerData.get(playerUuid);
        return data == null
                ? new NbtCompound()
                : data.copy();
    }

    public void writePlayerData(
            UUID playerUuid,
            NbtCompound data
    ) {
        playerData.put(
                Objects.requireNonNull(playerUuid, "playerUuid"),
                Objects.requireNonNull(data, "data").copy()
        );
        markDirty();
    }

    public boolean removePlayerData(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");

        boolean removed = playerData.remove(playerUuid) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public int playerRecordCount() {
        return playerData.size();
    }

    public int worldKeyCount() {
        return worldData.getSize();
    }
}
