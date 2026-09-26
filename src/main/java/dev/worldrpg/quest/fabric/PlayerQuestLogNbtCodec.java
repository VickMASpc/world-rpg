package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.quest.PlayerQuestLog;
import dev.worldrpg.quest.QuestProgress;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PlayerQuestLogNbtCodec {
    private static final String SCHEMA = "schema";
    private static final String ENTRIES = "entries";
    private static final String COMPLETED = "completed";
    private static final String COMPLETED_QUESTS = "completed_quests";
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

        NbtCompound completedQuests = new NbtCompound();
        for (RpgId questId : log.completedQuestIds()) {
            completedQuests.putInt(questId.toString(), 1);
        }
        root.put(COMPLETED_QUESTS, completedQuests);
        return root;
    }

    public static PlayerQuestLog decode(NbtCompound root) {
        if (root.getInt(SCHEMA) != CURRENT_SCHEMA) {
            throw new IllegalArgumentException(
                    "Unsupported quest-log schema: " + root.getInt(SCHEMA)
            );
        }

        List<QuestProgress> progress = new ArrayList<>();
        if (root.contains(ENTRIES, NbtElement.COMPOUND_TYPE)) {
            NbtCompound entries = root.getCompound(ENTRIES);
            for (String questIdText : entries.getKeys()) {
                if (!entries.contains(questIdText, NbtElement.COMPOUND_TYPE)) {
                    throw new IllegalArgumentException(
                            "Quest-log entry is not a compound: " + questIdText
                    );
                }

                RpgId questId = RpgId.parse(questIdText);
                NbtCompound quest = entries.getCompound(questIdText);
                Set<String> completedObjectives = new LinkedHashSet<>();
                if (quest.contains(COMPLETED, NbtElement.COMPOUND_TYPE)) {
                    completedObjectives.addAll(
                            quest.getCompound(COMPLETED).getKeys()
                    );
                }
                progress.add(new QuestProgress(questId, completedObjectives));
            }
        }

        Set<RpgId> completedQuests = new LinkedHashSet<>();
        if (root.contains(COMPLETED_QUESTS, NbtElement.COMPOUND_TYPE)) {
            for (String questIdText :
                    root.getCompound(COMPLETED_QUESTS).getKeys()) {
                completedQuests.add(RpgId.parse(questIdText));
            }
        }

        return new PlayerQuestLog(progress, completedQuests);
    }
}
