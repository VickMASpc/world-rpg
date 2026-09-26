package dev.worldrpg.content.enemy;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Objects;

public record MobContentDefinition(
        RpgId id,
        String displayName,
        int level,
        double maximumHealth,
        double attackDamage,
        double movementSpeed,
        RpgId minecraftEntityType,
        RequiredDefinitionRef<LootTableContentDefinition> lootTable,
        String sourceAsset
) implements RpgDefinition {
    public MobContentDefinition {
        Objects.requireNonNull(id, "id");
        displayName = requireText(displayName, "displayName");
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }
        if (!Double.isFinite(maximumHealth)
                || maximumHealth <= 0.0) {
            throw new IllegalArgumentException(
                    "maximumHealth must be finite and > 0"
            );
        }
        if (!Double.isFinite(attackDamage)
                || attackDamage < 0.0) {
            throw new IllegalArgumentException(
                    "attackDamage must be finite and >= 0"
            );
        }
        if (!Double.isFinite(movementSpeed)
                || movementSpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "movementSpeed must be finite and > 0"
            );
        }
        Objects.requireNonNull(
                minecraftEntityType,
                "minecraftEntityType"
        );
        Objects.requireNonNull(lootTable, "lootTable");
        sourceAsset = requireText(sourceAsset, "sourceAsset");
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        ReferenceResolver.resolve(
                lootTable,
                snapshot,
                report,
                source
        );
    }

    private static String requireText(
            String value,
            String field
    ) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return normalized;
    }
}
