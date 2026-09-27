package dev.worldrpg.quest;

import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;

import java.util.Objects;
import java.util.Optional;

/**
 * Defines the authored objective-order contract for adventure quests.
 */
public final class QuestObjectiveSequence {
    private QuestObjectiveSequence() {
    }

    public static Optional<QuestObjectiveSpec> firstIncomplete(
            QuestContentDefinition definition,
            QuestProgress progress
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(progress, "progress");

        if (!definition.id().equals(progress.questId())) {
            throw new IllegalArgumentException(
                    "quest progress " + progress.questId()
                            + " cannot evaluate definition " + definition.id()
            );
        }

        return definition.objectives()
                .stream()
                .filter(objective ->
                        !progress.isObjectiveComplete(objective.key())
                )
                .findFirst();
    }
}
