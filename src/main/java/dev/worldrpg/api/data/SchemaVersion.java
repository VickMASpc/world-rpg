package dev.worldrpg.api.data;

/**
 * Positive integer schema version for an authored definition domain.
 */
public record SchemaVersion(int value) implements Comparable<SchemaVersion> {
    public SchemaVersion {
        if (value < 1) {
            throw new IllegalArgumentException("schema version must be >= 1");
        }
    }

    @Override
    public int compareTo(SchemaVersion other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
