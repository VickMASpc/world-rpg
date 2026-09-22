package dev.worldrpg.content.load;

import com.google.gson.JsonObject;
import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.ReloadSafety;
import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionSchemaMigrationTest {
    private static final RegistryKey<Fixture> KEY =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/migration_fixture"),
                    Fixture.class
            );

    @Test
    void olderDefinitionMigratesBeforeDomainDecode() {
        DefinitionDomain<Fixture> domain =
                new DefinitionDomain<>(
                        KEY,
                        new SchemaVersion(2),
                        ReloadSafety.SAFE
                );

        DefinitionDomainHandler<Fixture> handler =
                DefinitionDomainHandler.of(
                        domain,
                        (document, report) -> {
                            if (!document.root().has("migrated")) {
                                report.error(
                                        "fixture.not_migrated",
                                        "fixture decoder expected migration output",
                                        document.source().sourceRef(),
                                        document.header().id()
                                );
                                return Optional.empty();
                            }
                            return Optional.of(
                                    new Fixture(document.header().id())
                            );
                        }
                );

        DefinitionDomainCatalog catalog =
                DefinitionDomainCatalog.builder()
                        .add(handler)
                        .addSchemaMigration(
                                KEY.id(),
                                migration(1, 2)
                        )
                        .build();

        ContentLoader loader = new ContentLoader(
                catalog,
                new RegistryPublisher()
        );

        var result = loader.loadAndPublish(
                List.of(source(1))
        );

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());
    }

    @Test
    void missingMigrationRejectsOlderDefinition() {
        DefinitionDomain<Fixture> domain =
                new DefinitionDomain<>(
                        KEY,
                        new SchemaVersion(2),
                        ReloadSafety.SAFE
                );

        ContentLoader loader = new ContentLoader(
                DefinitionDomainCatalog.builder()
                        .add(DefinitionDomainHandler.of(
                                domain,
                                (document, report) ->
                                        Optional.of(
                                                new Fixture(
                                                        document.header().id()
                                                )
                                        )
                        ))
                        .build(),
                new RegistryPublisher()
        );

        var result = loader.loadAndPublish(
                List.of(source(1))
        );

        assertFalse(result.published());
        assertTrue(result.report().messages().stream()
                .anyMatch(message ->
                        message.code().equals("schema.migration_required")
                ));
    }

    private static DefinitionSchemaMigration migration(
            int from,
            int to
    ) {
        return new DefinitionSchemaMigration() {
            @Override
            public SchemaVersion from() {
                return new SchemaVersion(from);
            }

            @Override
            public SchemaVersion to() {
                return new SchemaVersion(to);
            }

            @Override
            public JsonObject migrate(JsonObject input) {
                input.addProperty("migrated", true);
                return input;
            }
        };
    }

    private static ContentSource source(int schema) {
        return ContentSource.of(
                "fixture.json",
                """
                {
                  "schema": %d,
                  "registry": "world_rpg:registry/migration_fixture",
                  "id": "world_rpg:test/migration"
                }
                """.formatted(schema)
        );
    }

    private record Fixture(RpgId id)
            implements RpgDefinition {
    }
}
