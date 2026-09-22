package dev.worldrpg.persistence;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.registry.DefinitionRegistry;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistrySnapshot;

import java.util.Objects;
import java.util.Optional;

public final class PersistedReferenceResolver {
    private PersistedReferenceResolver() {
    }

    public static <T extends RpgDefinition>
    PersistedReferenceResolution<T> resolve(
            PersistedDefinitionPointer pointer,
            RegistryKey<T> expectedRegistry,
            RegistrySnapshot snapshot,
            MissingDefinitionPolicy policy
    ) {
        Objects.requireNonNull(pointer, "pointer");
        Objects.requireNonNull(expectedRegistry, "expectedRegistry");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(policy, "policy");

        if (!pointer.registryId().equals(expectedRegistry.id())) {
            throw new IllegalArgumentException(
                    "persisted reference registry mismatch: stored "
                            + pointer.registryId()
                            + ", expected "
                            + expectedRegistry.id()
            );
        }

        Optional<DefinitionRegistry<T>> registry =
                snapshot.find(expectedRegistry);
        Optional<T> definition = registry.flatMap(value ->
                value.find(pointer.definitionId())
        );

        if (definition.isPresent()) {
            return new PersistedReferenceResolution<>(
                    pointer,
                    definition,
                    PersistedReferenceStatus.RESOLVED
            );
        }

        return switch (policy) {
            case FAIL -> throw new IllegalStateException(
                    "missing persisted definition "
                            + pointer.definitionId()
                            + " in "
                            + pointer.registryId()
            );
            case PRESERVE_UNRESOLVED ->
                    new PersistedReferenceResolution<>(
                            pointer,
                            Optional.empty(),
                            PersistedReferenceStatus.PRESERVED_UNRESOLVED
                    );
            case DROP ->
                    new PersistedReferenceResolution<>(
                            pointer,
                            Optional.empty(),
                            PersistedReferenceStatus.DROPPED
                    );
        };
    }
}
