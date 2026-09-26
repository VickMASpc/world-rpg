package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record CombatSchoolKey(RpgId id)
        implements Comparable<CombatSchoolKey> {
    public CombatSchoolKey {
        Objects.requireNonNull(id, "id");
    }

    public static CombatSchoolKey of(String id) {
        return new CombatSchoolKey(RpgId.parse(id));
    }

    @Override
    public int compareTo(CombatSchoolKey other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
