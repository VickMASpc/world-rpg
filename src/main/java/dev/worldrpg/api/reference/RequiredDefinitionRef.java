package dev.worldrpg.api.reference;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;

import java.util.Objects;

/**
 * Reference field that must be present and resolve to a definition.
 */
public record RequiredDefinitionRef<T extends RpgDefinition>(
        RegistryKey<T> registry,
        RpgId id
) {
    public RequiredDefinitionRef {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
    }
}
