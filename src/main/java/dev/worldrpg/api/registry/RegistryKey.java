package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

/**
 * Typed identity of one definition registry domain.
 */
public record RegistryKey<T extends RpgDefinition>(RpgId id, Class<T> definitionType) {
    public RegistryKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(definitionType, "definitionType");
    }
}
