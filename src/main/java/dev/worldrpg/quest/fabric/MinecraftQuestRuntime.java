package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.player.PlayerRpgInventory;
import dev.worldrpg.player.fabric.PlayerRpgInventoryNbtCodec;
import dev.worldrpg.quest.PlayerQuestLog;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Optional;

public final class MinecraftQuestRuntime {
    private static final String QUEST_LOG = "quest_log";
    private static final String RPG_INVENTORY = "rpg_inventory";

    private MinecraftQuestRuntime() {
    }

    public static Optional<QuestContentDefinition> definition(RpgId questId) {
        return WorldRpgContentRuntime.publisher()
                .active()
                .find(AdventureContentDomains.QUESTS)
                .flatMap(registry -> registry.find(questId));
    }

    public static PlayerQuestLog load(ServerPlayerEntity player) {
        return decodeQuestLog(readPlayerData(player));
    }

    public static PlayerRpgInventory loadInventory(ServerPlayerEntity player) {
        return decodeInventory(readPlayerData(player));
    }

    public static AcceptResult accept(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        Optional<QuestContentDefinition> definition = definition(questId);
        if (definition.isEmpty()) {
            return AcceptResult.UNKNOWN_QUEST;
        }

        NbtCompound playerData = readPlayerData(player);
        PlayerQuestLog log = decodeQuestLog(playerData);

        if (log.hasCompleted(questId)) {
            return AcceptResult.ALREADY_COMPLETED;
        }
        boolean prerequisitesComplete = definition.orElseThrow()
                .prerequisites()
                .stream()
                .allMatch(prerequisite ->
                        log.hasCompleted(prerequisite.id())
                );
        if (!prerequisitesComplete) {
            return AcceptResult.PREREQUISITES_INCOMPLETE;
        }
        if (!log.accept(questId)) {
            return AcceptResult.ALREADY_ACTIVE;
        }

        playerData.put(QUEST_LOG, PlayerQuestLogNbtCodec.encode(log));
        writePlayerData(player, playerData);
        return AcceptResult.ACCEPTED;
    }

    public static AdvanceResult completeObjective(
            ServerPlayerEntity player,
            RpgId questId,
            String objectiveKey
    ) {
        Optional<QuestContentDefinition> definition = definition(questId);
        if (definition.isEmpty()) {
            return AdvanceResult.UNKNOWN_QUEST;
        }
        if (!definition.get().hasObjective(objectiveKey)) {
            return AdvanceResult.UNKNOWN_OBJECTIVE;
        }

        NbtCompound playerData = readPlayerData(player);
        PlayerQuestLog log = decodeQuestLog(playerData);

        if (log.find(questId).isEmpty()) {
            return log.hasCompleted(questId)
                    ? AdvanceResult.ALREADY_TURNED_IN
                    : AdvanceResult.NOT_ACTIVE;
        }

        if (!log.completeObjective(questId, objectiveKey)) {
            return AdvanceResult.ALREADY_COMPLETE;
        }

        playerData.put(QUEST_LOG, PlayerQuestLogNbtCodec.encode(log));
        writePlayerData(player, playerData);

        return log.find(questId)
                .orElseThrow()
                .readyToTurnIn(definition.get())
                ? AdvanceResult.READY_TO_TURN_IN
                : AdvanceResult.ADVANCED;
    }

    public static TurnInResult turnIn(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        Optional<QuestContentDefinition> definition = definition(questId);
        if (definition.isEmpty()) {
            return TurnInResult.UNKNOWN_QUEST;
        }

        NbtCompound playerData = readPlayerData(player);
        PlayerQuestLog log = decodeQuestLog(playerData);

        if (log.hasCompleted(questId)) {
            return TurnInResult.ALREADY_TURNED_IN;
        }

        var progress = log.find(questId);
        if (progress.isEmpty()) {
            return TurnInResult.NOT_ACTIVE;
        }
        if (!progress.get().readyToTurnIn(definition.get())) {
            return TurnInResult.OBJECTIVES_INCOMPLETE;
        }

        PlayerRpgInventory inventory = decodeInventory(playerData);
        for (var reward : definition.get().itemRewards()) {
            inventory.grantItem(
                    reward.item().id(),
                    reward.quantity()
            );
        }
        inventory.addCopper(definition.get().copperReward());

        if (!log.markTurnedIn(questId)) {
            throw new IllegalStateException(
                    "quest disappeared during turn-in: " + questId
            );
        }

        playerData.put(QUEST_LOG, PlayerQuestLogNbtCodec.encode(log));
        playerData.put(
                RPG_INVENTORY,
                PlayerRpgInventoryNbtCodec.encode(inventory)
        );
        writePlayerData(player, playerData);
        return TurnInResult.TURNED_IN;
    }

    public static QuestView view(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        Optional<QuestContentDefinition> definition = definition(questId);
        PlayerQuestLog log = load(player);

        if (definition.isEmpty()) {
            return new QuestView(
                    questId,
                    QuestState.UNRESOLVED_DEFINITION,
                    0,
                    0
            );
        }

        if (log.hasCompleted(questId)) {
            int total = definition.get().objectives().size();
            return new QuestView(
                    questId,
                    QuestState.COMPLETED,
                    total,
                    total
            );
        }

        var progress = log.find(questId);
        if (progress.isEmpty()) {
            return new QuestView(
                    questId,
                    QuestState.NOT_ACTIVE,
                    0,
                    definition.get().objectives().size()
            );
        }

        int completed = (int) definition.get()
                .objectives()
                .stream()
                .filter(objective ->
                        progress.get().isObjectiveComplete(objective.key())
                )
                .count();

        QuestState state = progress.get().readyToTurnIn(definition.get())
                ? QuestState.READY_TO_TURN_IN
                : QuestState.ACTIVE;

        return new QuestView(
                questId,
                state,
                completed,
                definition.get().objectives().size()
        );
    }

    private static NbtCompound readPlayerData(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");
        return WorldRpgPersistentState.get(player.getServer())
                .readPlayerData(player.getUuid());
    }

    private static void writePlayerData(
            ServerPlayerEntity player,
            NbtCompound playerData
    ) {
        WorldRpgPersistentState.get(player.getServer())
                .writePlayerData(player.getUuid(), playerData);
    }

    private static PlayerQuestLog decodeQuestLog(NbtCompound playerData) {
        if (!playerData.contains(QUEST_LOG, NbtElement.COMPOUND_TYPE)) {
            return new PlayerQuestLog();
        }
        return PlayerQuestLogNbtCodec.decode(
                playerData.getCompound(QUEST_LOG)
        );
    }

    private static PlayerRpgInventory decodeInventory(
            NbtCompound playerData
    ) {
        if (!playerData.contains(RPG_INVENTORY, NbtElement.COMPOUND_TYPE)) {
            return new PlayerRpgInventory();
        }
        return PlayerRpgInventoryNbtCodec.decode(
                playerData.getCompound(RPG_INVENTORY)
        );
    }

    public enum AcceptResult {
        ACCEPTED,
        ALREADY_ACTIVE,
        ALREADY_COMPLETED,
        PREREQUISITES_INCOMPLETE,
        UNKNOWN_QUEST
    }

    public enum AdvanceResult {
        ADVANCED,
        READY_TO_TURN_IN,
        ALREADY_COMPLETE,
        ALREADY_TURNED_IN,
        NOT_ACTIVE,
        UNKNOWN_OBJECTIVE,
        UNKNOWN_QUEST
    }

    public enum TurnInResult {
        TURNED_IN,
        OBJECTIVES_INCOMPLETE,
        NOT_ACTIVE,
        ALREADY_TURNED_IN,
        UNKNOWN_QUEST
    }

    public enum QuestState {
        NOT_ACTIVE,
        ACTIVE,
        READY_TO_TURN_IN,
        COMPLETED,
        UNRESOLVED_DEFINITION
    }

    public record QuestView(
            RpgId questId,
            QuestState state,
            int completedObjectives,
            int totalObjectives
    ) {
    }
}
