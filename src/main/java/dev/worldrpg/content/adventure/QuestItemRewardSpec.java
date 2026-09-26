package dev.worldrpg.content.adventure;

import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Objects;

public record QuestItemRewardSpec(
        RequiredDefinitionRef<ItemContentDefinition> item,
        int quantity
) {
    public QuestItemRewardSpec {
        Objects.requireNonNull(item, "item");
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "quest item reward quantity must be >= 1"
            );
        }
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        ReferenceResolver.resolve(item, snapshot, report, source);
    }
}
