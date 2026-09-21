package dev.worldrpg.api.data;

import dev.worldrpg.api.registry.RegistryKey;

import java.util.Objects;

/**
 * Metadata for one authored definition domain.
 */
public record DefinitionDomain<T extends RpgDefinition>(
        RegistryKey<T> registryKey,
        SchemaVersion currentSchema,
        ReloadSafety reloadSafety
) {
    public DefinitionDomain {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(currentSchema, "currentSchema");
        Objects.requireNonNull(reloadSafety, "reloadSafety");
    }
}
