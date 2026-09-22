package dev.worldrpg.combat.cooldown;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record CooldownKey(RpgId id) implements Comparable<CooldownKey> {
    public CooldownKey {
        Objects.requireNonNull(id, "id");
    }

    public static CooldownKey of(String id) {
        return new CooldownKey(RpgId.parse(id));
    }

    @Override
    public int compareTo(CooldownKey other) {
        return id.compareTo(other.id);
    }
}
