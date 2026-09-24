package dev.worldrpg.quest;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class QuestProgress {
    private final RpgId questId;
    private final Set<String> completedObjectives;

    public QuestProgress(RpgId questId) {
        this(questId, Set.of());
    }

    public QuestProgress(
            RpgId questId,
            Set<String> completedObjectives
    ) {
        this.questId = Objects.requireNonNull(questId, "questId");

        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String key : Objects.requireNonNull(
                completedObjectives,
                "completedObjectives"
        )) {
            copy.add(QuestObjectiveSpec.validateKey(key));
        }
        this.completedObjectives = copy;
    }

    public RpgId questId() {
        return questId;
    }

    public Set<String> completedObjectives() {
        return Set.copyOf(completedObjectives);
    }

    public boolean completeObjective(String key) {
        return completedObjectives.add(
                QuestObjectiveSpec.validateKey(key)
        );
    }

    public boolean isObjectiveComplete(String key) {
        return completedObjectives.contains(key);
    }

    public boolean readyToTurnIn(QuestContentDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        if (!definition.id().equals(questId)) {
            throw new IllegalArgumentException(
                    "quest progress " + questId
                            + " cannot evaluate definition " + definition.id()
            );
        }

        return definition.objectives()
                .stream()
                .allMatch(objective ->
                        completedObjectives.contains(objective.key())
                );
    }
}
