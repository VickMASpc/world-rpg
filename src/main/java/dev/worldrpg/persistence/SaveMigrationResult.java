package dev.worldrpg.persistence;

import java.util.Objects;

public record SaveMigrationResult<T>(
        SaveSchemaVersion sourceVersion,
        SaveSchemaVersion finalVersion,
        T value,
        int migrationsApplied
) {
    public SaveMigrationResult {
        Objects.requireNonNull(sourceVersion, "sourceVersion");
        Objects.requireNonNull(finalVersion, "finalVersion");
        Objects.requireNonNull(value, "value");

        if (migrationsApplied < 0) {
            throw new IllegalArgumentException(
                    "migrationsApplied must be >= 0"
            );
        }
    }
}
