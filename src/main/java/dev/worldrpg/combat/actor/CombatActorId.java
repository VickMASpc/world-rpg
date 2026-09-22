package dev.worldrpg.combat.actor;

public record CombatActorId(long value) implements Comparable<CombatActorId> {
    public CombatActorId {
        if (value < 1) {
            throw new IllegalArgumentException("combat actor id must be >= 1");
        }
    }

    @Override
    public int compareTo(CombatActorId other) {
        return Long.compare(value, other.value);
    }
}
