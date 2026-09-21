package dev.worldrpg.combat.aura;

public record AuraInstanceId(long value) implements Comparable<AuraInstanceId> {
    public AuraInstanceId {
        if (value < 1) {
            throw new IllegalArgumentException("aura instance id must be >= 1");
        }
    }

    @Override
    public int compareTo(AuraInstanceId other) {
        return Long.compare(value, other.value);
    }
}
