package dev.worldrpg.combat.resource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public final class ResourceSet {
    private final Map<ResourceKey, ResourcePool> pools = new LinkedHashMap<>();

    public ResourcePool add(ResourceKey key, double maximum, double current) {
        Objects.requireNonNull(key, "key");

        if (pools.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate resource pool: " + key);
        }

        ResourcePool pool = new ResourcePool(key, maximum, current);
        pools.put(key, pool);
        return pool;
    }

    public Optional<ResourcePool> find(ResourceKey key) {
        return Optional.ofNullable(pools.get(Objects.requireNonNull(key, "key")));
    }

    public ResourcePool require(ResourceKey key) {
        return find(key).orElseThrow(() ->
                new NoSuchElementException("Missing resource pool: " + key));
    }

    public Map<ResourceKey, ResourcePool> pools() {
        return Collections.unmodifiableMap(pools);
    }
}
