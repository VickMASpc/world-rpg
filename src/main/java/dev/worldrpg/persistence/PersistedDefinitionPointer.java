package dev.worldrpg.persistence;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record PersistedDefinitionPointer(
        RpgId registryId,
        RpgId definitionId
) {
    public PersistedDefinitionPointer {
        Objects.requireNonNull(registryId, "registryId");
        Objects.requireNonNull(definitionId, "definitionId");
    }
}
