package dev.worldrpg.persistence;

import dev.worldrpg.api.data.RpgDefinition;

import java.util.Objects;
import java.util.Optional;

public record PersistedReferenceResolution<T extends RpgDefinition>(
        PersistedDefinitionPointer pointer,
        Optional<T> definition,
        PersistedReferenceStatus status
) {
    public PersistedReferenceResolution {
        Objects.requireNonNull(pointer, "pointer");
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(status, "status");

        if (status == PersistedReferenceStatus.RESOLVED
                && definition.isEmpty()) {
            throw new IllegalArgumentException(
                    "resolved persisted reference requires definition"
            );
        }

        if (status != PersistedReferenceStatus.RESOLVED
                && definition.isPresent()) {
            throw new IllegalArgumentException(
                    "unresolved/dropped reference cannot carry definition"
            );
        }
    }
}
