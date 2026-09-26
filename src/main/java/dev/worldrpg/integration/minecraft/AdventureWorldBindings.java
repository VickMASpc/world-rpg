package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent bridge between authored RPG IDs and physical Minecraft entities
 * or world volumes.
 */
public final class AdventureWorldBindings {
    private static final String ROOT = "adventure_world_bindings";
    private static final String NPCS = "npcs";
    private static final String LOCATIONS = "locations";

    private static final String DIMENSION = "dimension";
    private static final String MIN_X = "min_x";
    private static final String MIN_Y = "min_y";
    private static final String MIN_Z = "min_z";
    private static final String MAX_X = "max_x";
    private static final String MAX_Y = "max_y";
    private static final String MAX_Z = "max_z";

    private final Map<UUID, RpgId> npcByEntity = new LinkedHashMap<>();
    private final Map<RpgId, LocationBinding> locations =
            new LinkedHashMap<>();

    private MinecraftServer server;

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        load();
    }

    public void stop() {
        server = null;
        npcByEntity.clear();
        locations.clear();
    }

    public void bindNpc(UUID entityUuid, RpgId npcId) {
        requireStarted();
        npcByEntity.put(
                Objects.requireNonNull(entityUuid, "entityUuid"),
                Objects.requireNonNull(npcId, "npcId")
        );
        persist();
    }

    public void unbindNpc(UUID entityUuid) {
        requireStarted();
        if (npcByEntity.remove(
                Objects.requireNonNull(entityUuid, "entityUuid")
        ) != null) {
            persist();
        }
    }

    public Optional<RpgId> npcId(UUID entityUuid) {
        requireStarted();
        return Optional.ofNullable(
                npcByEntity.get(
                        Objects.requireNonNull(entityUuid, "entityUuid")
                )
        );
    }

    public void bindLocation(
            RpgId locationId,
            ServerWorld world,
            int centerX,
            int centerY,
            int centerZ,
            int horizontalRadius,
            int verticalRadius
    ) {
        requireStarted();
        Objects.requireNonNull(locationId, "locationId");
        Objects.requireNonNull(world, "world");
        if (horizontalRadius < 0 || verticalRadius < 0) {
            throw new IllegalArgumentException(
                    "location binding radii must be >= 0"
            );
        }

        String dimension = world.getRegistryKey()
                .getValue()
                .toString();

        locations.put(
                locationId,
                new LocationBinding(
                        locationId,
                        dimension,
                        centerX - horizontalRadius,
                        centerY - verticalRadius,
                        centerZ - horizontalRadius,
                        centerX + horizontalRadius,
                        centerY + verticalRadius,
                        centerZ + horizontalRadius
                )
        );
        persist();
    }

    public void unbindLocation(RpgId locationId) {
        requireStarted();
        if (locations.remove(
                Objects.requireNonNull(locationId, "locationId")
        ) != null) {
            persist();
        }
    }

    public Set<RpgId> locationsContaining(
            ServerPlayerEntity player
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        String dimension = player.getServerWorld()
                .getRegistryKey()
                .getValue()
                .toString();
        var pos = player.getBlockPos();

        Set<RpgId> result = new LinkedHashSet<>();
        for (LocationBinding binding : locations.values()) {
            if (binding.contains(
                    dimension,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            )) {
                result.add(binding.locationId());
            }
        }
        return Set.copyOf(result);
    }

    public Optional<LocationBinding> locationBinding(
            RpgId locationId
    ) {
        requireStarted();
        return Optional.ofNullable(
                locations.get(
                        Objects.requireNonNull(
                                locationId,
                                "locationId"
                        )
                )
        );
    }

    public int npcBindingCount() {
        return npcByEntity.size();
    }

    public int locationBindingCount() {
        return locations.size();
    }

    private void load() {
        npcByEntity.clear();
        locations.clear();

        NbtCompound worldData =
                WorldRpgPersistentState.get(server).readWorldData();
        if (!worldData.contains(ROOT, NbtElement.COMPOUND_TYPE)) {
            return;
        }

        NbtCompound root = worldData.getCompound(ROOT);

        if (root.contains(NPCS, NbtElement.COMPOUND_TYPE)) {
            NbtCompound npcs = root.getCompound(NPCS);
            for (String uuidText : npcs.getKeys()) {
                if (!npcs.contains(
                        uuidText,
                        NbtElement.STRING_TYPE
                )) {
                    continue;
                }
                try {
                    npcByEntity.put(
                            UUID.fromString(uuidText),
                            RpgId.parse(npcs.getString(uuidText))
                    );
                } catch (IllegalArgumentException ignored) {
                    // Invalid stale bindings are ignored rather than
                    // preventing the world from loading.
                }
            }
        }

        if (root.contains(LOCATIONS, NbtElement.COMPOUND_TYPE)) {
            NbtCompound encoded = root.getCompound(LOCATIONS);
            for (String idText : encoded.getKeys()) {
                if (!encoded.contains(
                        idText,
                        NbtElement.COMPOUND_TYPE
                )) {
                    continue;
                }

                try {
                    RpgId locationId = RpgId.parse(idText);
                    NbtCompound value = encoded.getCompound(idText);
                    locations.put(
                            locationId,
                            new LocationBinding(
                                    locationId,
                                    value.getString(DIMENSION),
                                    value.getInt(MIN_X),
                                    value.getInt(MIN_Y),
                                    value.getInt(MIN_Z),
                                    value.getInt(MAX_X),
                                    value.getInt(MAX_Y),
                                    value.getInt(MAX_Z)
                            )
                    );
                } catch (IllegalArgumentException ignored) {
                    // Same policy as stale NPC bindings.
                }
            }
        }
    }

    private void persist() {
        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData = persistence.readWorldData();

        NbtCompound root = new NbtCompound();
        NbtCompound npcs = new NbtCompound();
        npcByEntity.forEach((uuid, npcId) ->
                npcs.putString(
                        uuid.toString(),
                        npcId.toString()
                )
        );
        root.put(NPCS, npcs);

        NbtCompound encodedLocations = new NbtCompound();
        locations.forEach((locationId, binding) -> {
            NbtCompound value = new NbtCompound();
            value.putString(DIMENSION, binding.dimension());
            value.putInt(MIN_X, binding.minX());
            value.putInt(MIN_Y, binding.minY());
            value.putInt(MIN_Z, binding.minZ());
            value.putInt(MAX_X, binding.maxX());
            value.putInt(MAX_Y, binding.maxY());
            value.putInt(MAX_Z, binding.maxZ());
            encodedLocations.put(
                    locationId.toString(),
                    value
            );
        });
        root.put(LOCATIONS, encodedLocations);

        worldData.put(ROOT, root);
        persistence.writeWorldData(worldData);
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "adventure world bindings are not started"
            );
        }
    }

    public record LocationBinding(
            RpgId locationId,
            String dimension,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        public LocationBinding {
            Objects.requireNonNull(locationId, "locationId");
            Objects.requireNonNull(dimension, "dimension");
            if (minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException(
                        "location binding minimum exceeds maximum"
                );
            }
        }

        public boolean contains(
                String dimension,
                int x,
                int y,
                int z
        ) {
            return this.dimension.equals(dimension)
                    && x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }
}
