package dev.worldrpg.content.adventure;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;

import java.util.List;
import java.util.Objects;

public record WorldLocationContentDefinition(
        RpgId id,
        String displayName,
        String kind,
        List<String> tags
) implements RpgDefinition {
    public WorldLocationContentDefinition {
        Objects.requireNonNull(id, "id");
        displayName = requireText(displayName, "displayName");
        kind = requireText(kind, "kind");
        tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        for (String tag : tags) {
            requireText(tag, "tag");
        }
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
