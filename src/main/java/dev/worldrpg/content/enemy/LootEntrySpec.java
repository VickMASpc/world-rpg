package dev.worldrpg.content.enemy;

import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.api.reference.RequiredDefinitionRef;

import java.util.Objects;

public record LootEntrySpec(
        RequiredDefinitionRef<ItemContentDefinition> item,
        int minimumQuantity,
        int maximumQuantity,
        double chance
) {
    public LootEntrySpec {
        Objects.requireNonNull(item, "item");
        if (minimumQuantity < 1) {
            throw new IllegalArgumentException(
                    "minimumQuantity must be >= 1"
            );
        }
        if (maximumQuantity < minimumQuantity) {
            throw new IllegalArgumentException(
                    "maximumQuantity must be >= minimumQuantity"
            );
        }
        if (!Double.isFinite(chance)
                || chance < 0.0
                || chance > 1.0) {
            throw new IllegalArgumentException(
                    "chance must be between 0 and 1"
            );
        }
    }
}
