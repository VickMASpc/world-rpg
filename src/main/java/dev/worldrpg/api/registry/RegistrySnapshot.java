package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable set of definition registries published as one coherent snapshot.
 */
public final class RegistrySnapshot {
    private static final RegistrySnapshot EMPTY = new RegistrySnapshot(Map.of());

    private final Map<RegistryKey<?>, DefinitionRegistry<?>> registries;

    private RegistrySnapshot(Map<RegistryKey<?>, DefinitionRegistry<?>> registries) {
        this.registries = Map.copyOf(new LinkedHashMap<>(registries));
    }

    public static RegistrySnapshot empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int registryCount() {
        return registries.size();
    }

    public Map<RegistryKey<?>, DefinitionRegistry<?>> registries() {
        return registries;
    }

    @SuppressWarnings("unchecked")
    public <T extends RpgDefinition> Optional<DefinitionRegistry<T>> find(RegistryKey<T> key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable((DefinitionRegistry<T>) registries.get(key));
    }

    public <T extends RpgDefinition> DefinitionRegistry<T> require(RegistryKey<T> key) {
        return find(key).orElseThrow(() ->
                new NoSuchElementException("Missing registry: " + key.id()));
    }

    public static final class Builder {
        private static final SourceRef INTERNAL_SOURCE = SourceRef.of("<registry-snapshot>");

        private final Map<RegistryKey<?>, DefinitionRegistry<?>> registries = new LinkedHashMap<>();

        public <T extends RpgDefinition> boolean add(
                DefinitionRegistry<T> registry,
                ValidationReport report
        ) {
            Objects.requireNonNull(registry, "registry");
            Objects.requireNonNull(report, "report");

            RegistryKey<T> key = registry.key();
            if (registries.containsKey(key)) {
                report.error(
                        "registry.duplicate_registry",
                        "Duplicate registry domain: " + key.id(),
                        INTERNAL_SOURCE
                );
                return false;
            }

            registries.put(key, registry);
            return true;
        }

        public RegistrySnapshot build() {
            return new RegistrySnapshot(registries);
        }
    }
}
