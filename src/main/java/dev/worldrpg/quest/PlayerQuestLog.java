package dev.worldrpg.quest;

import dev.worldrpg.api.id.RpgId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class PlayerQuestLog {
    private final Map<RpgId, QuestProgress> active =
            new LinkedHashMap<>();

    public PlayerQuestLog() {
    }

    public PlayerQuestLog(Collection<QuestProgress> progress) {
        for (QuestProgress entry : Objects.requireNonNull(
                progress,
                "progress"
        )) {
            QuestProgress previous = active.putIfAbsent(
                    entry.questId(),
                    new QuestProgress(
                            entry.questId(),
                            entry.completedObjectives()
                    )
            );
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate quest progress: " + entry.questId()
                );
            }
        }
    }

    public boolean accept(RpgId questId) {
        Objects.requireNonNull(questId, "questId");
        return active.putIfAbsent(
                questId,
                new QuestProgress(questId)
        ) == null;
    }

    public Optional<QuestProgress> find(RpgId questId) {
        return Optional.ofNullable(
                active.get(Objects.requireNonNull(questId, "questId"))
        );
    }

    public boolean completeObjective(
            RpgId questId,
            String objectiveKey
    ) {
        QuestProgress progress = active.get(
                Objects.requireNonNull(questId, "questId")
        );
        if (progress == null) {
            throw new IllegalStateException(
                    "quest is not active: " + questId
            );
        }
        return progress.completeObjective(objectiveKey);
    }

    public int activeCount() {
        return active.size();
    }

    public Set<RpgId> activeQuestIds() {
        return Set.copyOf(active.keySet());
    }

    public Collection<QuestProgress> entries() {
        return active.values()
                .stream()
                .map(entry -> new QuestProgress(
                        entry.questId(),
                        entry.completedObjectives()
                ))
                .toList();
    }
}
