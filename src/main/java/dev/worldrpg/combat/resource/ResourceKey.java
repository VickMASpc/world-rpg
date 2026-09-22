package dev.worldrpg.combat.resource;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record ResourceKey(RpgId id) implements Comparable<ResourceKey> {
    public ResourceKey {
        Objects.requireNonNull(id, "id");
    }

    public static ResourceKey of(String id) {
        return new ResourceKey(RpgId.parse(id));
    }

    @Override
    public int compareTo(ResourceKey other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
