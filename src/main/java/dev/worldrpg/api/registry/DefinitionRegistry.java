package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.SourceRef;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable registry of one definition type.
 */
public final class DefinitionRegistry<T extends RpgDefinition> {
    private final RegistryKey<T> key;
    private final Map<RpgId, T> definitions;
    private final Map<RpgId, SourceRef> sources;

    DefinitionRegistry(
            RegistryKey<T> key,
            Map<RpgId, T> definitions,
            Map<RpgId, SourceRef> sources
    ) {
        this.key = Objects.requireNonNull(key, "key");
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
        this.sources = Collections.unmodifiableMap(new LinkedHashMap<>(sources));
    }

    public RegistryKey<T> key() {
        return key;
    }

    public int size() {
        return definitions.size();
    }

    public Optional<T> find(RpgId id) {
        return Optional.ofNullable(definitions.get(Objects.requireNonNull(id, "id")));
    }

    public T require(RpgId id) {
        return find(id).orElseThrow(() ->
                new NoSuchElementException("Missing " + key.id() + " definition: " + id));
    }

    public Optional<SourceRef> sourceOf(RpgId id) {
        return Optional.ofNullable(sources.get(Objects.requireNonNull(id, "id")));
    }

    public Collection<T> values() {
        return definitions.values();
    }

    public Map<RpgId, T> asMap() {
        return definitions;
    }
}
