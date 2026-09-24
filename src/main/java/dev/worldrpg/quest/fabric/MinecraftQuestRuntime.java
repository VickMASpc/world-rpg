package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.quest.PlayerQuestLog;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Optional;

public final class MinecraftQuestRuntime {
    private static final String QUEST_LOG = "quest_log";

    private MinecraftQuestRuntime() {
    }

    public static Optional<QuestContentDefinition> definition(
            RpgId questId
    ) {
        return WorldRpgContentRuntime.publisher()
                .active()
                .find(AdventureContentDomains.QUESTS)
                .flatMap(registry -> registry.find(questId));
    }

    public static PlayerQuestLog load(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");

        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound playerData =
                state.readPlayerData(player.getUuid());

        if (!playerData.contains(
                QUEST_LOG,
                NbtElement.COMPOUND_TYPE
        )) {
            return new PlayerQuestLog();
        }

        return PlayerQuestLogNbtCodec.decode(
                playerData.getCompound(QUEST_LOG)
        );
    }

    public static void save(
            ServerPlayerEntity player,
            PlayerQuestLog log
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(log, "log");

        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound playerData =
                state.readPlayerData(player.getUuid());

        playerData.put(
                QUEST_LOG,
                PlayerQuestLogNbtCodec.encode(log)
        );
        state.writePlayerData(player.getUuid(), playerData);
    }

    public static AcceptResult accept(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        if (definition(questId).isEmpty()) {
            return AcceptResult.UNKNOWN_QUEST;
        }

        PlayerQuestLog log = load(player);
        if (!log.accept(questId)) {
            return AcceptResult.ALREADY_ACTIVE;
        }

        save(player, log);
        return AcceptResult.ACCEPTED;
    }

    public static AdvanceResult completeObjective(
            ServerPlayerEntity player,
            RpgId questId,
            String objectiveKey
    ) {
        Optional<QuestContentDefinition> definition =
                definition(questId);
        if (definition.isEmpty()) {
            return AdvanceResult.UNKNOWN_QUEST;
        }
        if (!definition.get().hasObjective(objectiveKey)) {
            return AdvanceResult.UNKNOWN_OBJECTIVE;
        }

        PlayerQuestLog log = load(player);
        if (log.find(questId).isEmpty()) {
            return AdvanceResult.NOT_ACTIVE;
        }

        if (!log.completeObjective(questId, objectiveKey)) {
            return AdvanceResult.ALREADY_COMPLETE;
        }

        save(player, log);

        return log.find(questId)
                .orElseThrow()
                .readyToTurnIn(definition.get())
                ? AdvanceResult.READY_TO_TURN_IN
                : AdvanceResult.ADVANCED;
    }

    public static QuestView view(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        Optional<QuestContentDefinition> definition =
                definition(questId);
        PlayerQuestLog log = load(player);

        if (definition.isEmpty()) {
            return new QuestView(
                    questId,
                    QuestState.UNRESOLVED_DEFINITION,
                    0,
                    0
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

        QuestState state = progress.get()
                .readyToTurnIn(definition.get())
                ? QuestState.READY_TO_TURN_IN
                : QuestState.ACTIVE;

        return new QuestView(
                questId,
                state,
                completed,
                definition.get().objectives().size()
        );
    }

    public enum AcceptResult {
        ACCEPTED,
        ALREADY_ACTIVE,
        UNKNOWN_QUEST
    }

    public enum AdvanceResult {
        ADVANCED,
        READY_TO_TURN_IN,
        ALREADY_COMPLETE,
        NOT_ACTIVE,
        UNKNOWN_OBJECTIVE,
        UNKNOWN_QUEST
    }

    public enum QuestState {
        NOT_ACTIVE,
        ACTIVE,
        READY_TO_TURN_IN,
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
