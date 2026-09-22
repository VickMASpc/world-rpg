package dev.worldrpg.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SaveMigrator<T> {
    private final SaveSchemaVersion current;
    private final Map<SaveSchemaVersion, SaveMigration<T>> migrations;

    public SaveMigrator(
            SaveSchemaVersion current,
            List<? extends SaveMigration<T>> migrations
    ) {
        this.current = Objects.requireNonNull(current, "current");
        Objects.requireNonNull(migrations, "migrations");

        Map<SaveSchemaVersion, SaveMigration<T>> bySource =
                new LinkedHashMap<>();

        for (SaveMigration<T> migration : migrations) {
            Objects.requireNonNull(migration, "migration");

            if (!migration.to().equals(migration.from().next())) {
                throw new IllegalArgumentException(
                        "save migration must advance exactly one version: "
                                + migration.from().value()
                                + " -> "
                                + migration.to().value()
                );
            }

            SaveMigration<T> previous =
                    bySource.putIfAbsent(migration.from(), migration);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate save migration from schema "
                                + migration.from().value()
                );
            }
        }

        this.migrations = Map.copyOf(bySource);
    }

    public SaveSchemaVersion current() {
        return current;
    }

    public SaveMigrationResult<T> migrate(
            T input,
            SaveSchemaVersion sourceVersion
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(sourceVersion, "sourceVersion");

        if (sourceVersion.compareTo(current) > 0) {
            throw new IllegalStateException(
                    "save schema "
                            + sourceVersion.value()
                            + " is newer than supported schema "
                            + current.value()
            );
        }

        T value = input;
        SaveSchemaVersion version = sourceVersion;
        int applied = 0;

        while (version.compareTo(current) < 0) {
            SaveMigration<T> migration = migrations.get(version);
            if (migration == null) {
                throw new IllegalStateException(
                        "missing save migration "
                                + version.value()
                                + " -> "
                                + version.next().value()
                );
            }

            value = Objects.requireNonNull(
                    migration.migrate(value),
                    "save migration returned null"
            );
            version = migration.to();
            applied++;
        }

        return new SaveMigrationResult<>(
                sourceVersion,
                version,
                value,
                applied
        );
    }
}
