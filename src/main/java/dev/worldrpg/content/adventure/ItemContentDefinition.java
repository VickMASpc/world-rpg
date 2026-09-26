package dev.worldrpg.content.adventure;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ItemContentDefinition(
        RpgId id,
        String displayName,
        Category category,
        int requiredLevel,
        long vendorValueCopper,
        List<String> tags,
        EquipmentSpec equipment
) implements RpgDefinition {
    public ItemContentDefinition {
        Objects.requireNonNull(id, "id");
        displayName = requireText(displayName, "displayName");
        Objects.requireNonNull(category, "category");
        if (requiredLevel < 1) {
            throw new IllegalArgumentException("requiredLevel must be >= 1");
        }
        if (vendorValueCopper < 0) {
            throw new IllegalArgumentException("vendorValueCopper must be >= 0");
        }
        tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        for (String tag : tags) {
            requireText(tag, "tag");
        }

        if (category == Category.EQUIPMENT && equipment == null) {
            throw new IllegalArgumentException(
                    "equipment-category items require an equipment spec"
            );
        }
        if (category != Category.EQUIPMENT && equipment != null) {
            throw new IllegalArgumentException(
                    "only equipment-category items may define equipment stats"
            );
        }
    }

    public ItemContentDefinition(
            RpgId id,
            String displayName,
            Category category,
            int requiredLevel,
            long vendorValueCopper,
            List<String> tags
    ) {
        this(
                id,
                displayName,
                category,
                requiredLevel,
                vendorValueCopper,
                tags,
                null
        );
    }

    public Optional<EquipmentSpec> equipmentSpec() {
        return Optional.ofNullable(equipment);
    }

    public enum Category {
        EQUIPMENT,
        CONSUMABLE,
        MATERIAL,
        QUEST,
        MISC
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
