package dev.worldrpg.combat.cast;

public record CastId(long value) implements Comparable<CastId> {
    public CastId {
        if (value < 1) {
            throw new IllegalArgumentException("cast id must be >= 1");
        }
    }

    @Override
    public int compareTo(CastId other) {
        return Long.compare(value, other.value);
    }
}
