package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.quest.PlayerQuestLog;
import dev.worldrpg.quest.QuestProgress;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.List;

public final class PlayerQuestLogNbtCodec {
    private static final String SCHEMA = "schema";
    private static final String ENTRIES = "entries";
    private static final String COMPLETED = "completed";
    private static final int CURRENT_SCHEMA = 1;

    private PlayerQuestLogNbtCodec() {
    }

    public static NbtCompound encode(PlayerQuestLog log) {
        NbtCompound root = new NbtCompound();
        root.putInt(SCHEMA, CURRENT_SCHEMA);

        NbtCompound entries = new NbtCompound();
        for (QuestProgress progress : log.entries()) {
            NbtCompound quest = new NbtCompound();
            NbtCompound completed = new NbtCompound();

            for (String key : progress.completedObjectives()) {
                completed.putInt(key, 1);
            }

            quest.put(COMPLETED, completed);
            entries.put(progress.questId().toString(), quest);
        }

        root.put(ENTRIES, entries);
        return root;
    }

    public static PlayerQuestLog decode(NbtCompound root) {
        if (root.getInt(SCHEMA) != CURRENT_SCHEMA) {
            throw new IllegalArgumentException(
                    "Unsupported quest-log schema: " + root.getInt(SCHEMA)
            );
        }

        if (!root.contains(ENTRIES, NbtElement.COMPOUND_TYPE)) {
            return new PlayerQuestLog();
        }

        NbtCompound entries = root.getCompound(ENTRIES);
        List<QuestProgress> progress = new ArrayList<>();

        for (String questIdText : entries.getKeys()) {
            if (!entries.contains(
                    questIdText,
                    NbtElement.COMPOUND_TYPE
            )) {
                throw new IllegalArgumentException(
                        "Quest-log entry is not a compound: " + questIdText
                );
            }

            RpgId questId = RpgId.parse(questIdText);
            NbtCompound quest = entries.getCompound(questIdText);
            java.util.LinkedHashSet<String> completed =
                    new java.util.LinkedHashSet<>();

            if (quest.contains(COMPLETED, NbtElement.COMPOUND_TYPE)) {
                NbtCompound completedNbt =
                        quest.getCompound(COMPLETED);
                completed.addAll(completedNbt.getKeys());
            }

            progress.add(new QuestProgress(questId, completed));
        }

        return new PlayerQuestLog(progress);
    }
}
