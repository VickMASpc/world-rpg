package dev.worldrpg.content.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.decode.DecodedJsonDocument;
import dev.worldrpg.content.decode.DefinitionHeader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefinitionSchemaMigrator {
    private final Map<SchemaVersion, DefinitionSchemaMigration> bySource;

    public DefinitionSchemaMigrator(
            List<? extends DefinitionSchemaMigration> migrations
    ) {
        Objects.requireNonNull(migrations, "migrations");

        Map<SchemaVersion, DefinitionSchemaMigration> map =
                new LinkedHashMap<>();

        for (DefinitionSchemaMigration migration : migrations) {
            Objects.requireNonNull(migration, "migration");

            if (!migration.to().equals(migration.from().next())) {
                throw new IllegalArgumentException(
                        "definition migration must advance exactly one schema: "
                                + migration.from().value()
                                + " -> "
                                + migration.to().value()
                );
            }

            if (map.putIfAbsent(migration.from(), migration) != null) {
                throw new IllegalArgumentException(
                        "duplicate definition migration from schema "
                                + migration.from().value()
                );
            }
        }

        bySource = Map.copyOf(map);
    }

    public Optional<DecodedJsonDocument> migrate(
            DecodedJsonDocument document,
            SchemaVersion target,
            ValidationReport report
    ) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(report, "report");

        SchemaVersion source = document.header().schema();

        if (source.compareTo(target) > 0) {
            report.error(
                    "schema.unsupported_newer",
                    "Definition schema "
                            + source.value()
                            + " is newer than supported schema "
                            + target.value(),
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }

        JsonObject root = document.root().deepCopy();
        SchemaVersion current = source;

        while (current.compareTo(target) < 0) {
            DefinitionSchemaMigration migration =
                    bySource.get(current);

            if (migration == null) {
                report.error(
                        "schema.migration_required",
                        "Missing definition migration "
                                + current.value()
                                + " -> "
                                + current.next().value(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }

            try {
                root = Objects.requireNonNull(
                        migration.migrate(root.deepCopy()),
                        "definition migration returned null"
                );
            } catch (RuntimeException exception) {
                report.error(
                        "schema.migration_failed",
                        "Definition migration "
                                + migration.from().value()
                                + " -> "
                                + migration.to().value()
                                + " failed: "
                                + (exception.getMessage() == null
                                        ? exception.getClass().getSimpleName()
                                        : exception.getMessage()),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }

            current = migration.to();
            root.addProperty("schema", current.value());

            if (!identityStillMatches(document, root)) {
                report.error(
                        "schema.migration_identity_changed",
                        "Definition migration may not change registry or stable definition ID",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }
        }

        return Optional.of(new DecodedJsonDocument(
                new DefinitionHeader(
                        target,
                        document.header().registry(),
                        document.header().id()
                ),
                root,
                document.source()
        ));
    }

    private static boolean identityStillMatches(
            DecodedJsonDocument original,
            JsonObject root
    ) {
        JsonElement registry = root.get("registry");
        JsonElement id = root.get("id");

        return registry != null
                && registry.isJsonPrimitive()
                && registry.getAsJsonPrimitive().isString()
                && registry.getAsString().equals(
                        original.header().registry().toString()
                )
                && id != null
                && id.isJsonPrimitive()
                && id.getAsJsonPrimitive().isString()
                && id.getAsString().equals(
                        original.header().id().toString()
                );
    }
}
