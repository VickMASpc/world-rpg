package dev.worldrpg.combat.stat;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record StatKey(RpgId id) implements Comparable<StatKey> {
    public StatKey {
        Objects.requireNonNull(id, "id");
    }

    public static StatKey of(String id) {
        return new StatKey(RpgId.parse(id));
    }

    @Override
    public int compareTo(StatKey other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
