package dev.worldrpg.content.enemy;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.List;
import java.util.Objects;

public record LootTableContentDefinition(
        RpgId id,
        List<LootEntrySpec> entries,
        long minimumCopper,
        long maximumCopper
) implements RpgDefinition {
    public LootTableContentDefinition {
        Objects.requireNonNull(id, "id");
        entries = List.copyOf(
                Objects.requireNonNull(entries, "entries")
        );
        if (minimumCopper < 0) {
            throw new IllegalArgumentException(
                    "minimumCopper must be >= 0"
            );
        }
        if (maximumCopper < minimumCopper) {
            throw new IllegalArgumentException(
                    "maximumCopper must be >= minimumCopper"
            );
        }
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        for (LootEntrySpec entry : entries) {
            ReferenceResolver.resolve(
                    entry.item(),
                    snapshot,
                    report,
                    source
            );
        }
    }
}
