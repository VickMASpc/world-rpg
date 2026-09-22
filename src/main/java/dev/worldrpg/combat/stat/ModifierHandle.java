package dev.worldrpg.combat.stat;

public record ModifierHandle(long value) implements Comparable<ModifierHandle> {
    public ModifierHandle {
        if (value < 1) {
            throw new IllegalArgumentException("modifier handle must be >= 1");
        }
    }

    @Override
    public int compareTo(ModifierHandle other) {
        return Long.compare(value, other.value);
    }
}
