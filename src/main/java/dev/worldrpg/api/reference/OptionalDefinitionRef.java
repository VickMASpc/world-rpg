package dev.worldrpg.api.reference;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;

import java.util.Objects;
import java.util.Optional;

/**
 * Reference field that may be absent. If present, the target must still resolve.
 */
public record OptionalDefinitionRef<T extends RpgDefinition>(
        RegistryKey<T> registry,
        Optional<RpgId> id
) {
    public OptionalDefinitionRef {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
    }

    public static <T extends RpgDefinition> OptionalDefinitionRef<T> absent(RegistryKey<T> registry) {
        return new OptionalDefinitionRef<>(registry, Optional.empty());
    }

    public static <T extends RpgDefinition> OptionalDefinitionRef<T> of(
            RegistryKey<T> registry,
            RpgId id
    ) {
        return new OptionalDefinitionRef<>(registry, Optional.of(id));
    }
}
