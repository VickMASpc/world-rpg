package dev.worldrpg.content.enemy;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.combat.AbilityContentDefinition;
import dev.worldrpg.content.combat.CombatContentDomains;

import java.util.Objects;

public record MobContentDefinition(
        RpgId id,
        String displayName,
        int level,
        double maximumHealth,
        double attackDamage,
        double movementSpeed,
        double aggroRange,
        double assistRange,
        double attackRange,
        RpgId minecraftEntityType,
        RequiredDefinitionRef<LootTableContentDefinition> lootTable,
        RequiredDefinitionRef<AbilityContentDefinition> primaryAbility,
        RequiredDefinitionRef<AbilityContentDefinition> engageAbility,
        String sourceAsset
) implements RpgDefinition {
    public MobContentDefinition {
        Objects.requireNonNull(id, "id");
        displayName = requireText(displayName, "displayName");
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }
        requirePositive(maximumHealth, "maximumHealth");
        requireNonNegative(attackDamage, "attackDamage");
        requirePositive(movementSpeed, "movementSpeed");
        requirePositive(aggroRange, "aggroRange");
        requireNonNegative(assistRange, "assistRange");
        requirePositive(attackRange, "attackRange");
        if (attackRange > aggroRange) {
            throw new IllegalArgumentException(
                    "attackRange must be <= aggroRange"
            );
        }
        Objects.requireNonNull(
                minecraftEntityType,
                "minecraftEntityType"
        );
        Objects.requireNonNull(lootTable, "lootTable");
        Objects.requireNonNull(primaryAbility, "primaryAbility");
        Objects.requireNonNull(engageAbility, "engageAbility");
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
        ReferenceResolver.resolve(
                primaryAbility,
                snapshot,
                report,
                source
        );
        ReferenceResolver.resolve(
                engageAbility,
                snapshot,
                report,
                source
        );
    }

    public static RequiredDefinitionRef<AbilityContentDefinition>
    abilityRef(RpgId id) {
        return new RequiredDefinitionRef<>(
                CombatContentDomains.ABILITIES,
                id
        );
    }

    private static void requirePositive(
            double value,
            String field
    ) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(
                    field + " must be finite and > 0"
            );
        }
    }

    private static void requireNonNegative(
            double value,
            String field
    ) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    field + " must be finite and >= 0"
            );
        }
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
