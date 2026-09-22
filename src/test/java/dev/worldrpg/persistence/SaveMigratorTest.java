package dev.worldrpg.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaveMigratorTest {
    @Test
    void appliesEveryContiguousMigrationExactlyOnce() {
        SaveMigrator<String> migrator = new SaveMigrator<>(
                new SaveSchemaVersion(3),
                List.of(
                        migration(1, 2, value -> value + "-v2"),
                        migration(2, 3, value -> value + "-v3")
                )
        );

        SaveMigrationResult<String> result = migrator.migrate(
                "start",
                new SaveSchemaVersion(1)
        );

        assertEquals("start-v2-v3", result.value());
        assertEquals(3, result.finalVersion().value());
        assertEquals(2, result.migrationsApplied());
    }

    @Test
    void newerSaveAndMissingMigrationFailLoudly() {
        SaveMigrator<String> missing = new SaveMigrator<>(
                new SaveSchemaVersion(3),
                List.of(migration(1, 2, value -> value))
        );

        assertThrows(
                IllegalStateException.class,
                () -> missing.migrate("x", new SaveSchemaVersion(2))
        );

        assertThrows(
                IllegalStateException.class,
                () -> missing.migrate("x", new SaveSchemaVersion(4))
        );
    }

    @Test
    void migrationMustAdvanceOneVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SaveMigrator<>(
                        new SaveSchemaVersion(3),
                        List.of(migration(1, 3, value -> value))
                )
        );
    }

    private static SaveMigration<String> migration(
            int from,
            int to,
            java.util.function.UnaryOperator<String> operation
    ) {
        return new SaveMigration<>() {
            @Override
            public SaveSchemaVersion from() {
                return new SaveSchemaVersion(from);
            }

            @Override
            public SaveSchemaVersion to() {
                return new SaveSchemaVersion(to);
            }

            @Override
            public String migrate(String input) {
                return operation.apply(input);
            }
        };
    }
}
