package dev.worldrpg.quest;

import dev.worldrpg.api.id.RpgId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class PlayerQuestLog {
    private final Map<RpgId, QuestProgress> active = new LinkedHashMap<>();
    private final Set<RpgId> completed = new LinkedHashSet<>();

    public PlayerQuestLog() {
    }

    public PlayerQuestLog(Collection<QuestProgress> progress) {
        this(progress, Set.of());
    }

    public PlayerQuestLog(
            Collection<QuestProgress> progress,
            Collection<RpgId> completed
    ) {
        for (QuestProgress entry : Objects.requireNonNull(progress, "progress")) {
            QuestProgress previous = active.putIfAbsent(
                    entry.questId(),
                    new QuestProgress(entry.questId(), entry.completedObjectives())
            );
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate quest progress: " + entry.questId()
                );
            }
        }

        for (RpgId questId : Objects.requireNonNull(completed, "completed")) {
            Objects.requireNonNull(questId, "completed questId");
            if (active.containsKey(questId)) {
                throw new IllegalArgumentException(
                        "quest cannot be active and completed: " + questId
                );
            }
            this.completed.add(questId);
        }
    }

    public boolean accept(RpgId questId) {
        Objects.requireNonNull(questId, "questId");
        if (completed.contains(questId)) return false;
        return active.putIfAbsent(questId, new QuestProgress(questId)) == null;
    }

    public Optional<QuestProgress> find(RpgId questId) {
        return Optional.ofNullable(
                active.get(Objects.requireNonNull(questId, "questId"))
        );
    }

    public boolean completeObjective(RpgId questId, String objectiveKey) {
        QuestProgress progress = active.get(
                Objects.requireNonNull(questId, "questId")
        );
        if (progress == null) {
            throw new IllegalStateException("quest is not active: " + questId);
        }
        return progress.completeObjective(objectiveKey);
    }

    public boolean markTurnedIn(RpgId questId) {
        Objects.requireNonNull(questId, "questId");
        QuestProgress removed = active.remove(questId);
        if (removed == null) return false;
        completed.add(questId);
        return true;
    }

    public boolean hasCompleted(RpgId questId) {
        return completed.contains(
                Objects.requireNonNull(questId, "questId")
        );
    }

    public int activeCount() {
        return active.size();
    }

    public int completedCount() {
        return completed.size();
    }

    public Set<RpgId> activeQuestIds() {
        return Set.copyOf(active.keySet());
    }

    public Set<RpgId> completedQuestIds() {
        return Set.copyOf(completed);
    }

    public Collection<QuestProgress> entries() {
        return active.values().stream()
                .map(entry -> new QuestProgress(
                        entry.questId(),
                        entry.completedObjectives()
                ))
                .toList();
    }
}
