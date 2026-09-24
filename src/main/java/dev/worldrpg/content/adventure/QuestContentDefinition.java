package dev.worldrpg.content.adventure;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record QuestContentDefinition(
        RpgId id,
        String title,
        String journalSummary,
        int minimumLevel,
        RequiredDefinitionRef<NpcContentDefinition> starter,
        RequiredDefinitionRef<NpcContentDefinition> turnIn,
        List<QuestObjectiveSpec> objectives,
        List<QuestItemRewardSpec> itemRewards,
        long copperReward
) implements RpgDefinition {
    public QuestContentDefinition {
        Objects.requireNonNull(id, "id");
        title = requireText(title, "title");
        journalSummary = requireText(journalSummary, "journalSummary");
        if (minimumLevel < 1) {
            throw new IllegalArgumentException("minimumLevel must be >= 1");
        }
        Objects.requireNonNull(starter, "starter");
        Objects.requireNonNull(turnIn, "turnIn");
        objectives = List.copyOf(Objects.requireNonNull(objectives, "objectives"));
        itemRewards = List.copyOf(Objects.requireNonNull(itemRewards, "itemRewards"));
        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("quest must contain at least one objective");
        }
        Set<String> objectiveKeys = new HashSet<>();
        for (QuestObjectiveSpec objective : objectives) {
            if (!objectiveKeys.add(objective.key())) {
                throw new IllegalArgumentException(
                        "duplicate quest objective key: " + objective.key()
                );
            }
        }
        if (copperReward < 0) {
            throw new IllegalArgumentException("copperReward must be >= 0");
        }
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        ReferenceResolver.resolve(starter, snapshot, report, source);
        ReferenceResolver.resolve(turnIn, snapshot, report, source);
        for (QuestObjectiveSpec objective : objectives) {
            objective.resolveReferences(snapshot, source, report);
        }
        for (QuestItemRewardSpec reward : itemRewards) {
            reward.resolveReferences(snapshot, source, report);
        }
    }

    public boolean hasObjective(String key) {
        return objectives.stream()
                .anyMatch(objective -> objective.key().equals(key));
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
