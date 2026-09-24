package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable set of definition registries published as one coherent snapshot.
 *
 * <p>Registry-domain IDs are globally unique inside a snapshot, independent of
 * the Java definition type associated with the key.</p>
 */
public final class RegistrySnapshot {
    private static final RegistrySnapshot EMPTY = new RegistrySnapshot(Map.of());

    private final Map<RpgId, DefinitionRegistry<?>> registries;

    private RegistrySnapshot(Map<RpgId, DefinitionRegistry<?>> registries) {
        this.registries = Collections.unmodifiableMap(new LinkedHashMap<>(registries));
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

    public Map<RpgId, DefinitionRegistry<?>> registries() {
        return registries;
    }

    @SuppressWarnings("unchecked")
    public <T extends RpgDefinition> Optional<DefinitionRegistry<T>> find(RegistryKey<T> key) {
        Objects.requireNonNull(key, "key");

        DefinitionRegistry<?> registry = registries.get(key.id());
        if (registry == null) {
            return Optional.empty();
        }

        if (!registry.key().equals(key)) {
            throw new IllegalArgumentException(
                    "Registry key type mismatch for " + key.id()
                            + ": active type is " + registry.key().definitionType().getName()
                            + ", requested type is " + key.definitionType().getName()
            );
        }

        return Optional.of((DefinitionRegistry<T>) registry);
    }

    public <T extends RpgDefinition> DefinitionRegistry<T> require(RegistryKey<T> key) {
        return find(key).orElseThrow(() ->
                new NoSuchElementException("Missing registry: " + key.id()));
    }

    public static final class Builder {
        private static final SourceRef INTERNAL_SOURCE = SourceRef.of("<registry-snapshot>");

        private final Map<RpgId, DefinitionRegistry<?>> registries = new LinkedHashMap<>();

        public <T extends RpgDefinition> boolean add(
                DefinitionRegistry<T> registry,
                ValidationReport report
        ) {
            Objects.requireNonNull(registry, "registry");
            Objects.requireNonNull(report, "report");

            RpgId registryId = registry.key().id();
            DefinitionRegistry<?> existing = registries.get(registryId);
            if (existing != null) {
                report.error(
                        "registry.duplicate_registry",
                        "Duplicate registry domain " + registryId
                                + "; existing type is " + existing.key().definitionType().getName()
                                + ", duplicate type is " + registry.key().definitionType().getName(),
                        INTERNAL_SOURCE
                );
                return false;
            }

            registries.put(registryId, registry);
            return true;
        }

        public RegistrySnapshot build() {
            return new RegistrySnapshot(registries);
        }
    }
}
