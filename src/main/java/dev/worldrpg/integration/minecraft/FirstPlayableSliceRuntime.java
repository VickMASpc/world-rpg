package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * First end-to-end World RPG playable slice.
 *
 * <p>The command layer only creates the disposable physical scene. Quest
 * acceptance, visit progression, reporting and turn-in happen through real
 * movement and entity interaction.</p>
 */
public final class FirstPlayableSliceRuntime {
    public static final RpgId QUEST_ID = RpgId.parse(
            "world_rpg:quest/first_province/east_road_disappearances"
    );
    public static final RpgId WARDEN_ID = RpgId.parse(
            "world_rpg:npc/first_province/road_warden"
    );
    public static final RpgId CHECKPOINT_LOCATION_ID = RpgId.parse(
            "world_rpg:location/first_province/east_road_checkpoint"
    );

    public static final String INSPECT_ROUTE = "inspect_route";
    public static final String REPORT_TO_WARDEN = "report_to_warden";

    private static final String WORLD_KEY = "first_playable_slice";
    private static final String ORIGIN_X = "origin_x";
    private static final String ORIGIN_Y = "origin_y";
    private static final String ORIGIN_Z = "origin_z";
    private static final String CHECKPOINT_X = "checkpoint_x";
    private static final String CHECKPOINT_Y = "checkpoint_y";
    private static final String CHECKPOINT_Z = "checkpoint_z";
    private static final String WARDEN_UUID = "warden_uuid";

    private static final int CHECKPOINT_EAST = 48;
    private static final int CHECKPOINT_RADIUS = 5;
    private static final int TICK_INTERVAL = 10;

    private MinecraftServer server;
    private int ticks;
    private boolean interactionRegistered;

    public void registerInteraction() {
        if (interactionRegistered) {
            return;
        }
        interactionRegistered = true;

        UseEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {
                    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return ActionResult.PASS;
                    }
                    if (hand != Hand.MAIN_HAND) {
                        return ActionResult.PASS;
                    }
                    return interact(serverPlayer, entity)
                            ? ActionResult.SUCCESS
                            : ActionResult.PASS;
                }
        );
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        ticks = 0;
    }

    public void stop() {
        server = null;
        ticks = 0;
    }

    public void tick(MinecraftServer server) {
        if (this.server != server) {
            return;
        }
        ticks++;
        if (ticks % TICK_INTERVAL != 0) {
            return;
        }

        Optional<SliceState> state = loadState();
        if (state.isEmpty()) {
            return;
        }

        for (ServerPlayerEntity player
                : server.getPlayerManager().getPlayerList()) {
            if (!player.getServerWorld()
                    .getRegistryKey()
                    .equals(World.OVERWORLD)) {
                continue;
            }
            maybeCompleteCheckpointVisit(player, state.orElseThrow());
        }
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

        QuestContentDefinition definition = requireContentContract();

        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        int originX = playerPos.getX();
        int originZ = playerPos.getZ();
        int originY = surfaceY(world, originX, originZ);

        int changedBlocks = buildHomePost(world, originX, originZ);
        changedBlocks += buildRoad(world, originX, originZ);

        int checkpointX = originX + CHECKPOINT_EAST;
        int checkpointZ = originZ;
        int checkpointY = surfaceY(world, checkpointX, checkpointZ);
        changedBlocks += buildCheckpoint(
                world,
                checkpointX,
                checkpointZ
        );

        VillagerEntity warden = EntityType.VILLAGER.spawn(
                world,
                new BlockPos(originX, originY + 1, originZ),
                SpawnReason.COMMAND
        );
        if (warden == null) {
            throw new IllegalStateException(
                    "failed to spawn the Road Warden"
            );
        }

        warden.setAiDisabled(true);
        warden.setPersistent();
        warden.setInvulnerable(true);
        warden.setCustomName(Text.literal("Road Warden"));
        warden.setCustomNameVisible(true);

        SliceState state = new SliceState(
                originX,
                originY,
                originZ,
                checkpointX,
                checkpointY,
                checkpointZ,
                warden.getUuid()
        );
        saveState(state);

        return new BuildResult(
                state,
                changedBlocks,
                definition.title()
        );
    }

    public boolean reset() {
        requireStarted();

        Optional<SliceState> state = loadState();
        if (state.isEmpty()) {
            return false;
        }

        MinecraftEntityResolver.findLiving(
                server,
                state.orElseThrow().wardenUuid()
        ).ifPresent(Entity::discard);

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

        var quest = MinecraftQuestRuntime.view(player, QUEST_ID);
        var inventory = MinecraftQuestRuntime.loadInventory(player);

        return "first playable slice | origin="
                + value.originX() + "," + value.originY() + "," + value.originZ()
                + " checkpoint="
                + value.checkpointX() + "," + value.checkpointY() + "," + value.checkpointZ()
                + " wardenAvailable=" + wardenAvailable
                + " quest=" + quest.state()
                + " objectives=" + quest.completedObjectives()
                + "/" + quest.totalObjectives()
                + " copper=" + inventory.copper()
                + " items=" + inventory.totalItemCount();
    }

    private boolean interact(
            ServerPlayerEntity player,
            Entity entity
    ) {
        if (server == null) {
            return false;
        }

        Optional<SliceState> state = loadState();
        if (state.isEmpty()
                || !state.orElseThrow().wardenUuid().equals(entity.getUuid())) {
            return false;
        }

        QuestContentDefinition definition;
        try {
            definition = requireContentContract();
        } catch (IllegalStateException exception) {
            player.sendMessage(
                    Text.literal(
                            "World RPG playable slice is unavailable: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return true;
        }

        var view = MinecraftQuestRuntime.view(player, QUEST_ID);

        switch (view.state()) {
            case NOT_ACTIVE -> acceptFromWarden(player, definition);
            case ACTIVE -> continueConversation(player);
            case READY_TO_TURN_IN -> turnInToWarden(player, definition);
            case COMPLETED -> player.sendMessage(
                    Text.literal(
                            "Road Warden: The east road is still watched. "
                                    + "What you found bought us time."
                    ),
                    false
            );
            case UNRESOLVED_DEFINITION -> player.sendMessage(
                    Text.literal(
                            "Road Warden cannot resolve the quest definition. "
                                    + "Check /worldrpg content status."
                    ),
                    false
            );
        }

        return true;
    }

    private void acceptFromWarden(
            ServerPlayerEntity player,
            QuestContentDefinition definition
    ) {
        var result = MinecraftQuestRuntime.accept(player, QUEST_ID);
        if (result != MinecraftQuestRuntime.AcceptResult.ACCEPTED) {
            player.sendMessage(
                    Text.literal(
                            "Road Warden interaction failed to accept quest: "
                                    + result
                    ),
                    false
            );
            return;
        }

        player.sendMessage(
                Text.literal(
                        "Road Warden: Three travelers vanished on the east road. "
                                + "Follow the worn track, inspect the abandoned checkpoint, "
                                + "then come straight back to me."
                ),
                false
        );
        player.sendMessage(
                Text.literal("Quest accepted: " + definition.title()),
                false
        );
        player.sendMessage(
                Text.literal(
                        "Objective: follow the dirt road east and inspect the checkpoint."
                ),
                false
        );
    }

    private void continueConversation(ServerPlayerEntity player) {
        var progress = MinecraftQuestRuntime.load(player)
                .find(QUEST_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "active quest view has no quest progress"
                ));

        if (!progress.isObjectiveComplete(INSPECT_ROUTE)) {
            player.sendMessage(
                    Text.literal(
                            "Road Warden: The checkpoint is east of here. "
                                    + "Stay on the road and look around the old signal fire."
                    ),
                    false
            );
            return;
        }

        if (!progress.isObjectiveComplete(REPORT_TO_WARDEN)) {
            var result = MinecraftQuestRuntime.completeObjective(
                    player,
                    QUEST_ID,
                    REPORT_TO_WARDEN
            );

            player.sendMessage(
                    Text.literal(
                            "Road Warden: Empty bedrolls, a dead signal fire, "
                                    + "and tracks leaving the road... that is enough. "
                                    + "I'll send word to the next refuge."
                    ),
                    false
            );
            player.sendMessage(
                    Text.literal(
                            "Objective complete: report your findings to the Road Warden."
                    ),
                    false
            );

            if (result == MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN) {
                player.sendMessage(
                        Text.literal(
                                "Quest ready to turn in. Speak to the Road Warden again."
                        ),
                        false
                );
            }
            return;
        }

        player.sendMessage(
                Text.literal(
                        "Road Warden: I have your report. Speak to me again for your pay."
                ),
                false
        );
    }

    private void turnInToWarden(
            ServerPlayerEntity player,
            QuestContentDefinition definition
    ) {
        var result = MinecraftQuestRuntime.turnIn(player, QUEST_ID);
        if (result != MinecraftQuestRuntime.TurnInResult.TURNED_IN) {
            player.sendMessage(
                    Text.literal(
                            "Quest turn-in failed: " + result
                    ),
                    false
            );
            return;
        }

        player.sendMessage(
                Text.literal(
                        "Road Warden: Take this. The road has already cost enough people."
                ),
                false
        );
        player.sendMessage(
                Text.literal(
                        "Quest complete: " + definition.title()
                ),
                false
        );
        player.sendMessage(
                Text.literal(
                        "Reward: " + rewardSummary(definition)
                ),
                false
        );

        var inventory = MinecraftQuestRuntime.loadInventory(player);
        player.sendMessage(
                Text.literal(
                        "RPG inventory: copper=" + inventory.copper()
                                + " totalItems=" + inventory.totalItemCount()
                ),
                false
        );
    }

    private void maybeCompleteCheckpointVisit(
            ServerPlayerEntity player,
            SliceState state
    ) {
        BlockPos pos = player.getBlockPos();
        long dx = (long) pos.getX() - state.checkpointX();
        long dy = (long) pos.getY() - (state.checkpointY() + 1L);
        long dz = (long) pos.getZ() - state.checkpointZ();

        if (Math.abs(dy) > 6
                || dx * dx + dz * dz
                > (long) CHECKPOINT_RADIUS * CHECKPOINT_RADIUS) {
            return;
        }

        var view = MinecraftQuestRuntime.view(player, QUEST_ID);
        if (view.state() != MinecraftQuestRuntime.QuestState.ACTIVE) {
            return;
        }

        var progress = MinecraftQuestRuntime.load(player)
                .find(QUEST_ID)
                .orElse(null);
        if (progress == null
                || progress.isObjectiveComplete(INSPECT_ROUTE)) {
            return;
        }

        var result = MinecraftQuestRuntime.completeObjective(
                player,
                QUEST_ID,
                INSPECT_ROUTE
        );
        if (result != MinecraftQuestRuntime.AdvanceResult.ADVANCED
                && result != MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN) {
            return;
        }

        player.sendMessage(
                Text.literal(
                        "You inspect the abandoned checkpoint: cold ashes, "
                                + "discarded bedrolls, and tracks cutting away from the road."
                ),
                false
        );
        player.sendMessage(
                Text.literal(
                        "Objective complete: inspect the east-road checkpoint."
                ),
                false
        );
        player.sendMessage(
                Text.literal(
                        "New objective: return to the Road Warden and report what you found."
                ),
                false
        );
    }

    private QuestContentDefinition requireContentContract() {
        QuestContentDefinition definition = MinecraftQuestRuntime.definition(
                QUEST_ID
        ).orElseThrow(() -> new IllegalStateException(
                "quest definition is not loaded: " + QUEST_ID
        ));

        if (!definition.starter().id().equals(WARDEN_ID)
                || !definition.turnIn().id().equals(WARDEN_ID)) {
            throw new IllegalStateException(
                    "quest starter/turn-in no longer matches the physical Road Warden"
            );
        }

        QuestObjectiveSpec inspect = definition.objectives()
                .stream()
                .filter(objective -> objective.key().equals(INSPECT_ROUTE))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "quest is missing objective " + INSPECT_ROUTE
                ));

        if (!(inspect instanceof QuestObjectiveSpec.VisitLocation visit)
                || !visit.location().id().equals(CHECKPOINT_LOCATION_ID)) {
            throw new IllegalStateException(
                    "inspect_route no longer targets "
                            + CHECKPOINT_LOCATION_ID
            );
        }

        QuestObjectiveSpec report = definition.objectives()
                .stream()
                .filter(objective -> objective.key().equals(REPORT_TO_WARDEN))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "quest is missing objective " + REPORT_TO_WARDEN
                ));

        if (!(report instanceof QuestObjectiveSpec.SpeakToNpc speak)
                || !speak.npc().id().equals(WARDEN_ID)) {
            throw new IllegalStateException(
                    "report_to_warden no longer targets " + WARDEN_ID
            );
        }

        return definition;
    }

    private static String rewardSummary(
            QuestContentDefinition definition
    ) {
        String items = definition.itemRewards()
                .stream()
                .map(reward -> {
                    String name = WorldRpgContentRuntime.publisher()
                            .active()
                            .require(AdventureContentDomains.ITEMS)
                            .find(reward.item().id())
                            .map(ItemContentDefinition::displayName)
                            .orElse(reward.item().id().toString());
                    return name + " x" + reward.quantity();
                })
                .reduce((left, right) -> left + ", " + right)
                .orElse("no item reward");

        return items + ", " + definition.copperReward() + " copper";
    }

    private static int buildHomePost(
            ServerWorld world,
            int originX,
            int originZ
    ) {
        int changed = 0;
        for (int east = -3; east <= 3; east++) {
            for (int south = -3; south <= 3; south++) {
                int x = originX + east;
                int z = originZ + south;
                int y = surfaceY(world, x, z);
                world.setBlockState(
                        new BlockPos(x, y, z),
                        Blocks.COARSE_DIRT.getDefaultState(),
                        3
                );
                changed++;
            }
        }

        int y = surfaceY(world, originX - 2, originZ);
        world.setBlockState(
                new BlockPos(originX - 2, y + 1, originZ),
                Blocks.OAK_FENCE.getDefaultState(),
                3
        );
        world.setBlockState(
                new BlockPos(originX - 2, y + 2, originZ),
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
        for (int east = 4; east <= CHECKPOINT_EAST; east++) {
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
            int checkpointX,
            int checkpointZ
    ) {
        int changed = 0;
        for (int east = -3; east <= 3; east++) {
            for (int south = -3; south <= 3; south++) {
                int x = checkpointX + east;
                int z = checkpointZ + south;
                int y = surfaceY(world, x, z);
                world.setBlockState(
                        new BlockPos(x, y, z),
                        Blocks.GRAVEL.getDefaultState(),
                        3
                );
                changed++;
            }
        }

        int centerY = surfaceY(world, checkpointX, checkpointZ);
        for (int height = 1; height <= 3; height++) {
            world.setBlockState(
                    new BlockPos(
                            checkpointX,
                            centerY + height,
                            checkpointZ
                    ),
                    Blocks.MOSSY_COBBLESTONE.getDefaultState(),
                    3
            );
            changed++;
        }

        int fireX = checkpointX + 2;
        int fireZ = checkpointZ + 1;
        int fireY = surfaceY(world, fireX, fireZ);
        world.setBlockState(
                new BlockPos(fireX, fireY + 1, fireZ),
                Blocks.CAMPFIRE.getDefaultState(),
                3
        );
        return changed + 1;
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
        if (!worldData.contains(WORLD_KEY, NbtElement.COMPOUND_TYPE)) {
            return Optional.empty();
        }

        NbtCompound slice = worldData.getCompound(WORLD_KEY);
        if (!slice.contains(WARDEN_UUID, NbtElement.STRING_TYPE)) {
            throw new IllegalStateException(
                    "persisted playable-slice state is missing warden UUID"
            );
        }

        final UUID wardenUuid;
        try {
            wardenUuid = UUID.fromString(slice.getString(WARDEN_UUID));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "persisted playable-slice warden UUID is invalid",
                    exception
            );
        }

        return Optional.of(new SliceState(
                slice.getInt(ORIGIN_X),
                slice.getInt(ORIGIN_Y),
                slice.getInt(ORIGIN_Z),
                slice.getInt(CHECKPOINT_X),
                slice.getInt(CHECKPOINT_Y),
                slice.getInt(CHECKPOINT_Z),
                wardenUuid
        ));
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
        slice.putString(WARDEN_UUID, state.wardenUuid().toString());

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
            UUID wardenUuid
    ) {
    }

    public record BuildResult(
            SliceState state,
            int changedBlocks,
            String questTitle
    ) {
        public String summary() {
            return "first playable slice built | quest="
                    + questTitle
                    + " origin="
                    + state.originX() + "," + state.originY() + "," + state.originZ()
                    + " checkpoint="
                    + state.checkpointX() + "," + state.checkpointY() + "," + state.checkpointZ()
                    + " changedBlocks=" + changedBlocks
                    + " | right-click the Road Warden to begin";
        }
    }
}
