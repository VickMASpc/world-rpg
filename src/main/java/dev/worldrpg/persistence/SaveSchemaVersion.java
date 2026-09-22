package dev.worldrpg.persistence;

public record SaveSchemaVersion(int value)
        implements Comparable<SaveSchemaVersion> {
    public SaveSchemaVersion {
        if (value < 1) {
            throw new IllegalArgumentException(
                    "save schema version must be >= 1"
            );
        }
    }

    @Override
    public int compareTo(SaveSchemaVersion other) {
        return Integer.compare(value, other.value);
    }

    public SaveSchemaVersion next() {
        return new SaveSchemaVersion(Math.addExact(value, 1));
    }
}
