package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Disposable physical fixture for the first adventure-runtime production
 * slice. Quest progression itself is owned by {@link AdventureWorldRuntime}.
 */
public final class FirstPlayableSliceRuntime {
    public static final RpgId FIRST_QUEST_ID = RpgId.parse(
            "world_rpg:quest/first_province/east_road_disappearances"
    );
    public static final RpgId SECOND_QUEST_ID = RpgId.parse(
            "world_rpg:quest/first_province/waystation_silence"
    );

    public static final RpgId WARDEN_ID = RpgId.parse(
            "world_rpg:npc/first_province/road_warden"
    );
    public static final RpgId SCOUT_ID = RpgId.parse(
            "world_rpg:npc/first_province/refuge_scout"
    );

    public static final RpgId CHECKPOINT_LOCATION_ID = RpgId.parse(
            "world_rpg:location/first_province/east_road_checkpoint"
    );
    public static final RpgId REFUGE_LOCATION_ID = RpgId.parse(
            "world_rpg:location/first_province/east_road_refuge"
    );
    public static final RpgId WAYSTATION_LOCATION_ID = RpgId.parse(
            "world_rpg:location/first_province/collapsed_waystation"
    );

    private static final String WORLD_KEY = "first_playable_slice";
    private static final String ORIGIN_X = "origin_x";
    private static final String ORIGIN_Y = "origin_y";
    private static final String ORIGIN_Z = "origin_z";
    private static final String CHECKPOINT_X = "checkpoint_x";
    private static final String CHECKPOINT_Y = "checkpoint_y";
    private static final String CHECKPOINT_Z = "checkpoint_z";
    private static final String REFUGE_X = "refuge_x";
    private static final String REFUGE_Y = "refuge_y";
    private static final String REFUGE_Z = "refuge_z";
    private static final String WAYSTATION_X = "waystation_x";
    private static final String WAYSTATION_Y = "waystation_y";
    private static final String WAYSTATION_Z = "waystation_z";
    private static final String WARDEN_UUID = "warden_uuid";
    private static final String SCOUT_UUID = "scout_uuid";

    private static final int CHECKPOINT_EAST = 48;
    private static final int REFUGE_EAST = 104;
    private static final int WAYSTATION_EAST = 160;

    private final AdventureWorldRuntime adventureWorld;
    private MinecraftServer server;

    public FirstPlayableSliceRuntime(
            AdventureWorldRuntime adventureWorld
    ) {
        this.adventureWorld = Objects.requireNonNull(
                adventureWorld,
                "adventureWorld"
        );
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        restoreBindings();
    }

    public void stop() {
        server = null;
    }

    public BuildResult build(ServerPlayerEntity player) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        if (!player.getServerWorld()
                .getRegistryKey()
                .equals(World.OVERWORLD)) {
            throw new IllegalStateException(
                    "the first playable slice can only be built in the Overworld"
            );
        }
        if (loadState().isPresent()) {
            throw new IllegalStateException(
                    "a first playable slice already exists; use /worldrpg slice reset confirm before rebuilding"
            );
        }

        QuestContentDefinition first = requireQuestContract(
                FIRST_QUEST_ID,
                WARDEN_ID,
                CHECKPOINT_LOCATION_ID,
                "inspect_route",
                "report_to_warden"
        );
        QuestContentDefinition second = requireQuestContract(
                SECOND_QUEST_ID,
                SCOUT_ID,
                WAYSTATION_LOCATION_ID,
                "inspect_waystation",
                "report_to_scout"
        );

        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        int originX = playerPos.getX();
        int originZ = playerPos.getZ();
        int originY = surfaceY(world, originX, originZ);

        int changedBlocks = buildHomePost(
                world,
                originX,
                originZ
        );
        changedBlocks += buildRoad(
                world,
                originX,
                originZ
        );

        int checkpointX = originX + CHECKPOINT_EAST;
        int checkpointZ = originZ;
        int checkpointY = surfaceY(
                world,
                checkpointX,
                checkpointZ
        );
        changedBlocks += buildCheckpoint(
                world,
                checkpointX,
                checkpointZ
        );

        int refugeX = originX + REFUGE_EAST;
        int refugeZ = originZ;
        int refugeY = surfaceY(world, refugeX, refugeZ);
        changedBlocks += buildRefuge(
                world,
                refugeX,
                refugeZ
        );

        int waystationX = originX + WAYSTATION_EAST;
        int waystationZ = originZ;
        int waystationY = surfaceY(
                world,
                waystationX,
                waystationZ
        );
        changedBlocks += buildWaystation(
                world,
                waystationX,
                waystationZ
        );

        VillagerEntity warden = spawnNpc(
                world,
                new BlockPos(
                        originX,
                        originY + 1,
                        originZ - 2
                ),
                "Road Warden"
        );

        VillagerEntity scout;
        try {
            scout = spawnNpc(
                    world,
                    new BlockPos(
                            refugeX,
                            refugeY + 1,
                            refugeZ - 2
                    ),
                    "Refuge Scout"
            );
        } catch (RuntimeException exception) {
            warden.discard();
            throw exception;
        }

        SliceState state = new SliceState(
                originX,
                originY,
                originZ,
                checkpointX,
                checkpointY,
                checkpointZ,
                refugeX,
                refugeY,
                refugeZ,
                waystationX,
                waystationY,
                waystationZ,
                warden.getUuid(),
                scout.getUuid(),
                true
        );
        saveState(state);
        bind(state, world);

        return new BuildResult(
                state,
                changedBlocks,
                first.title(),
                second.title()
        );
    }

    public boolean reset() {
        requireStarted();

        Optional<SliceState> state = loadState();
        if (state.isEmpty()) {
            return false;
        }

        SliceState value = state.orElseThrow();
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld != null) {
            overworld.getChunk(
                    value.originX() >> 4,
                    value.originZ() >> 4
            );
            if (value.expanded()) {
                overworld.getChunk(
                        value.refugeX() >> 4,
                        value.refugeZ() >> 4
                );
            }
        }

        removeEntity(value.wardenUuid());
        adventureWorld.bindings().unbindNpc(
                value.wardenUuid()
        );
        adventureWorld.bindings().unbindLocation(
                CHECKPOINT_LOCATION_ID
        );

        if (value.expanded()) {
            removeEntity(value.scoutUuid());
            adventureWorld.bindings().unbindNpc(
                    value.scoutUuid()
            );
            adventureWorld.bindings().unbindLocation(
                    REFUGE_LOCATION_ID
            );
            adventureWorld.bindings().unbindLocation(
                    WAYSTATION_LOCATION_ID
            );
        }

        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData = persistence.readWorldData();
        worldData.remove(WORLD_KEY);
        persistence.writeWorldData(worldData);
        return true;
    }

    public String statusSummary(ServerPlayerEntity player) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        Optional<SliceState> state = loadState();
        if (state.isEmpty()) {
            return "first playable slice: NOT BUILT";
        }

        SliceState value = state.orElseThrow();
        boolean wardenAvailable = MinecraftEntityResolver.findLiving(
                server,
                value.wardenUuid()
        ).isPresent();

        var first = MinecraftQuestRuntime.view(
                player,
                FIRST_QUEST_ID
        );
        var inventory = MinecraftQuestRuntime.loadInventory(player);

        String summary = "first playable slice | origin="
                + value.originX() + ","
                + value.originY() + ","
                + value.originZ()
                + " checkpoint="
                + value.checkpointX() + ","
                + value.checkpointY() + ","
                + value.checkpointZ()
                + " wardenAvailable="
                + wardenAvailable
                + " quest1=" + first.state()
                + " objectives1="
                + first.completedObjectives()
                + "/" + first.totalObjectives();

        if (value.expanded()) {
            boolean scoutAvailable = MinecraftEntityResolver.findLiving(
                    server,
                    value.scoutUuid()
            ).isPresent();
            var second = MinecraftQuestRuntime.view(
                    player,
                    SECOND_QUEST_ID
            );
            summary += " refuge="
                    + value.refugeX() + ","
                    + value.refugeY() + ","
                    + value.refugeZ()
                    + " waystation="
                    + value.waystationX() + ","
                    + value.waystationY() + ","
                    + value.waystationZ()
                    + " scoutAvailable="
                    + scoutAvailable
                    + " quest2=" + second.state()
                    + " objectives2="
                    + second.completedObjectives()
                    + "/" + second.totalObjectives();
        } else {
            summary += " legacyFixture=true";
        }

        return summary
                + " copper=" + inventory.copper()
                + " items=" + inventory.totalItemCount()
                + " | "
                + adventureWorld.statusSummary();
    }

    private void restoreBindings() {
        Optional<SliceState> state = loadState();
        if (state.isEmpty()) {
            return;
        }

        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            return;
        }

        bind(state.orElseThrow(), overworld);
    }

    private void bind(
            SliceState state,
            ServerWorld world
    ) {
        adventureWorld.bindings().bindNpc(
                state.wardenUuid(),
                WARDEN_ID
        );
        adventureWorld.bindings().bindLocation(
                CHECKPOINT_LOCATION_ID,
                world,
                state.checkpointX(),
                state.checkpointY() + 1,
                state.checkpointZ(),
                5,
                6
        );

        if (!state.expanded()) {
            return;
        }

        adventureWorld.bindings().bindNpc(
                state.scoutUuid(),
                SCOUT_ID
        );
        adventureWorld.bindings().bindLocation(
                REFUGE_LOCATION_ID,
                world,
                state.refugeX(),
                state.refugeY() + 1,
                state.refugeZ(),
                6,
                6
        );
        adventureWorld.bindings().bindLocation(
                WAYSTATION_LOCATION_ID,
                world,
                state.waystationX(),
                state.waystationY() + 1,
                state.waystationZ(),
                5,
                6
        );
    }

    private QuestContentDefinition requireQuestContract(
            RpgId questId,
            RpgId npcId,
            RpgId locationId,
            String visitKey,
            String speakKey
    ) {
        QuestContentDefinition definition =
                MinecraftQuestRuntime.definition(
                        questId
                ).orElseThrow(() -> new IllegalStateException(
                        "quest definition is not loaded: "
                                + questId
                ));

        if (!definition.starter().id().equals(npcId)
                || !definition.turnIn().id().equals(npcId)) {
            throw new IllegalStateException(
                    questId
                            + " starter/turn-in does not match "
                            + npcId
            );
        }

        if (definition.objectives().size() != 2) {
            throw new IllegalStateException(
                    questId
                            + " physical fixture expects exactly two objectives"
            );
        }

        QuestObjectiveSpec first =
                definition.objectives().get(0);
        if (!(first instanceof QuestObjectiveSpec.VisitLocation visit)
                || !visit.key().equals(visitKey)
                || !visit.location().id().equals(locationId)) {
            throw new IllegalStateException(
                    questId
                            + " first objective no longer matches physical location "
                            + locationId
            );
        }

        QuestObjectiveSpec second =
                definition.objectives().get(1);
        if (!(second instanceof QuestObjectiveSpec.SpeakToNpc speak)
                || !speak.key().equals(speakKey)
                || !speak.npc().id().equals(npcId)) {
            throw new IllegalStateException(
                    questId
                            + " second objective no longer matches NPC "
                            + npcId
            );
        }

        return definition;
    }

    private static VillagerEntity spawnNpc(
            ServerWorld world,
            BlockPos position,
            String name
    ) {
        VillagerEntity npc = EntityType.VILLAGER.spawn(
                world,
                position,
                SpawnReason.COMMAND
        );
        if (npc == null) {
            throw new IllegalStateException(
                    "failed to spawn " + name
            );
        }

        npc.setAiDisabled(true);
        npc.setPersistent();
        npc.setInvulnerable(true);
        npc.setCustomName(Text.literal(name));
        npc.setCustomNameVisible(true);
        return npc;
    }

    private void removeEntity(UUID uuid) {
        if (uuid == null) {
            return;
        }
        MinecraftEntityResolver.findLiving(
                server,
                uuid
        ).ifPresent(Entity::discard);
    }

    private static int buildHomePost(
            ServerWorld world,
            int originX,
            int originZ
    ) {
        int changed = fillSurface(
                world,
                originX,
                originZ,
                3,
                Blocks.COARSE_DIRT
        );

        int y = surfaceY(world, originX - 2, originZ);
        world.setBlockState(
                new BlockPos(
                        originX - 2,
                        y + 1,
                        originZ
                ),
                Blocks.OAK_FENCE.getDefaultState(),
                3
        );
        world.setBlockState(
                new BlockPos(
                        originX - 2,
                        y + 2,
                        originZ
                ),
                Blocks.LANTERN.getDefaultState(),
                3
        );
        return changed + 2;
    }

    private static int buildRoad(
            ServerWorld world,
            int originX,
            int originZ
    ) {
        int changed = 0;
        for (int east = 4;
             east <= WAYSTATION_EAST;
             east++) {
            for (int south = -1; south <= 1; south++) {
                int x = originX + east;
                int z = originZ + south;
                int y = surfaceY(world, x, z);
                world.setBlockState(
                        new BlockPos(x, y, z),
                        Blocks.DIRT_PATH.getDefaultState(),
                        3
                );
                changed++;
            }
        }
        return changed;
    }

    private static int buildCheckpoint(
            ServerWorld world,
            int x,
            int z
    ) {
        int changed = fillSurface(
                world,
                x,
                z,
                3,
                Blocks.GRAVEL
        );

        int centerY = surfaceY(world, x, z);
        for (int height = 1; height <= 3; height++) {
            world.setBlockState(
                    new BlockPos(
                            x,
                            centerY + height,
                            z
                    ),
                    Blocks.MOSSY_COBBLESTONE
                            .getDefaultState(),
                    3
            );
            changed++;
        }

        int fireY = surfaceY(world, x + 2, z + 1);
        world.setBlockState(
                new BlockPos(
                        x + 2,
                        fireY + 1,
                        z + 1
                ),
                Blocks.CAMPFIRE.getDefaultState(),
                3
        );
        return changed + 1;
    }

    private static int buildRefuge(
            ServerWorld world,
            int x,
            int z
    ) {
        int changed = fillSurface(
                world,
                x,
                z,
                4,
                Blocks.STONE_BRICKS
        );

        for (int offset : new int[]{-3, 3}) {
            int y = surfaceY(world, x, z + offset);
            world.setBlockState(
                    new BlockPos(x, y + 1, z + offset),
                    Blocks.OAK_FENCE.getDefaultState(),
                    3
            );
            world.setBlockState(
                    new BlockPos(x, y + 2, z + offset),
                    Blocks.LANTERN.getDefaultState(),
                    3
            );
            changed += 2;
        }

        int barrelY = surfaceY(world, x + 2, z + 2);
        world.setBlockState(
                new BlockPos(x + 2, barrelY + 1, z + 2),
                Blocks.BARREL.getDefaultState(),
                3
        );
        return changed + 1;
    }

    private static int buildWaystation(
            ServerWorld world,
            int x,
            int z
    ) {
        int changed = fillSurface(
                world,
                x,
                z,
                4,
                Blocks.COARSE_DIRT
        );

        int y = surfaceY(world, x, z);
        for (int height = 1; height <= 4; height++) {
            world.setBlockState(
                    new BlockPos(
                            x,
                            y + height,
                            z
                    ),
                    height == 4
                            ? Blocks.CRACKED_STONE_BRICKS
                            .getDefaultState()
                            : Blocks.MOSSY_STONE_BRICKS
                            .getDefaultState(),
                    3
            );
            changed++;
        }

        int rubbleY = surfaceY(world, x + 2, z);
        world.setBlockState(
                new BlockPos(x + 2, rubbleY + 1, z),
                Blocks.MOSSY_COBBLESTONE.getDefaultState(),
                3
        );
        world.setBlockState(
                new BlockPos(x - 2, rubbleY + 1, z + 1),
                Blocks.COBBLESTONE.getDefaultState(),
                3
        );
        return changed + 2;
    }

    private static int fillSurface(
            ServerWorld world,
            int centerX,
            int centerZ,
            int radius,
            net.minecraft.block.Block block
    ) {
        int changed = 0;
        for (int east = -radius; east <= radius; east++) {
            for (int south = -radius;
                 south <= radius;
                 south++) {
                int x = centerX + east;
                int z = centerZ + south;
                int y = surfaceY(world, x, z);
                world.setBlockState(
                        new BlockPos(x, y, z),
                        block.getDefaultState(),
                        3
                );
                changed++;
            }
        }
        return changed;
    }

    private static int surfaceY(
            ServerWorld world,
            int x,
            int z
    ) {
        return world.getTopY(
                Heightmap.Type.WORLD_SURFACE,
                x,
                z
        ) - 1;
    }

    private Optional<SliceState> loadState() {
        requireStarted();

        NbtCompound worldData =
                WorldRpgPersistentState.get(server).readWorldData();
        if (!worldData.contains(
                WORLD_KEY,
                NbtElement.COMPOUND_TYPE
        )) {
            return Optional.empty();
        }

        NbtCompound slice = worldData.getCompound(WORLD_KEY);
        UUID wardenUuid = readUuid(
                slice,
                WARDEN_UUID,
                true
        );

        boolean expanded = slice.contains(
                SCOUT_UUID,
                NbtElement.STRING_TYPE
        );
        UUID scoutUuid = expanded
                ? readUuid(slice, SCOUT_UUID, true)
                : null;

        return Optional.of(new SliceState(
                slice.getInt(ORIGIN_X),
                slice.getInt(ORIGIN_Y),
                slice.getInt(ORIGIN_Z),
                slice.getInt(CHECKPOINT_X),
                slice.getInt(CHECKPOINT_Y),
                slice.getInt(CHECKPOINT_Z),
                expanded ? slice.getInt(REFUGE_X) : 0,
                expanded ? slice.getInt(REFUGE_Y) : 0,
                expanded ? slice.getInt(REFUGE_Z) : 0,
                expanded ? slice.getInt(WAYSTATION_X) : 0,
                expanded ? slice.getInt(WAYSTATION_Y) : 0,
                expanded ? slice.getInt(WAYSTATION_Z) : 0,
                wardenUuid,
                scoutUuid,
                expanded
        ));
    }

    private static UUID readUuid(
            NbtCompound compound,
            String key,
            boolean required
    ) {
        if (!compound.contains(
                key,
                NbtElement.STRING_TYPE
        )) {
            if (!required) {
                return null;
            }
            throw new IllegalStateException(
                    "persisted playable-slice state is missing "
                            + key
            );
        }

        try {
            return UUID.fromString(
                    compound.getString(key)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "persisted playable-slice UUID is invalid: "
                            + key,
                    exception
            );
        }
    }

    private void saveState(SliceState state) {
        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData = persistence.readWorldData();

        NbtCompound slice = new NbtCompound();
        slice.putInt(ORIGIN_X, state.originX());
        slice.putInt(ORIGIN_Y, state.originY());
        slice.putInt(ORIGIN_Z, state.originZ());
        slice.putInt(CHECKPOINT_X, state.checkpointX());
        slice.putInt(CHECKPOINT_Y, state.checkpointY());
        slice.putInt(CHECKPOINT_Z, state.checkpointZ());
        slice.putString(
                WARDEN_UUID,
                state.wardenUuid().toString()
        );

        if (state.expanded()) {
            slice.putInt(REFUGE_X, state.refugeX());
            slice.putInt(REFUGE_Y, state.refugeY());
            slice.putInt(REFUGE_Z, state.refugeZ());
            slice.putInt(
                    WAYSTATION_X,
                    state.waystationX()
            );
            slice.putInt(
                    WAYSTATION_Y,
                    state.waystationY()
            );
            slice.putInt(
                    WAYSTATION_Z,
                    state.waystationZ()
            );
            slice.putString(
                    SCOUT_UUID,
                    state.scoutUuid().toString()
            );
        }

        worldData.put(WORLD_KEY, slice);
        persistence.writeWorldData(worldData);
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "first playable slice runtime is not started"
            );
        }
    }

    public record SliceState(
            int originX,
            int originY,
            int originZ,
            int checkpointX,
            int checkpointY,
            int checkpointZ,
            int refugeX,
            int refugeY,
            int refugeZ,
            int waystationX,
            int waystationY,
            int waystationZ,
            UUID wardenUuid,
            UUID scoutUuid,
            boolean expanded
    ) {
    }

    public record BuildResult(
            SliceState state,
            int changedBlocks,
            String firstQuestTitle,
            String secondQuestTitle
    ) {
        public String summary() {
            return "adventure slice built | quest1="
                    + firstQuestTitle
                    + " quest2="
                    + secondQuestTitle
                    + " origin="
                    + state.originX() + ","
                    + state.originY() + ","
                    + state.originZ()
                    + " checkpoint="
                    + state.checkpointX() + ","
                    + state.checkpointY() + ","
                    + state.checkpointZ()
                    + " refuge="
                    + state.refugeX() + ","
                    + state.refugeY() + ","
                    + state.refugeZ()
                    + " waystation="
                    + state.waystationX() + ","
                    + state.waystationY() + ","
                    + state.waystationZ()
                    + " changedBlocks="
                    + changedBlocks
                    + " | Road Warden and Refuge Scout are driven by the generic adventure runtime";
        }
    }
}
