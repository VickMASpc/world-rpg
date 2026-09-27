package dev.worldrpg.content.adventure;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.List;
import java.util.Objects;

public record NpcContentDefinition(
        RpgId id,
        String displayName,
        RequiredDefinitionRef<WorldLocationContentDefinition> homeLocation,
        List<String> roles
) implements RpgDefinition {
    public NpcContentDefinition {
        Objects.requireNonNull(id, "id");
        displayName = requireText(displayName, "displayName");
        Objects.requireNonNull(homeLocation, "homeLocation");
        roles = List.copyOf(Objects.requireNonNull(roles, "roles"));
        for (String role : roles) {
            requireText(role, "role");
        }
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        ReferenceResolver.resolve(homeLocation, snapshot, report, source);
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
