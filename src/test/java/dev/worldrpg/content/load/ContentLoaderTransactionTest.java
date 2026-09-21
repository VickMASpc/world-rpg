package dev.worldrpg.content.load;

import com.google.gson.JsonElement;
import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.ReloadSafety;
import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.OptionalDefinitionRef;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.decode.DecodedJsonDocument;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentLoaderTransactionTest {
    private static final RegistryKey<FixtureDefinition> FIXTURES = new RegistryKey<>(
            RpgId.parse("world_rpg:registry/fixtures"),
            FixtureDefinition.class
    );

    private static final DefinitionDomain<FixtureDefinition> DOMAIN =
            new DefinitionDomain<>(
                    FIXTURES,
                    new SchemaVersion(1),
                    ReloadSafety.SAFE
            );

    @Test
    void validCandidatePublishesAfterReferenceResolution() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = loader(publisher);

        ContentLoadResult result = loader.loadAndPublish(List.of(
                source("a.json", "world_rpg:test/a", "world_rpg:test/b"),
                source("b.json", "world_rpg:test/b", null)
        ));

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());
        assertEquals(2, publisher.active().require(FIXTURES).size());
    }

    @Test
    void missingReferenceRejectsWholeCandidateAndPreservesLastKnownGood() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = loader(publisher);

        ContentLoadResult good = loader.loadAndPublish(List.of(
                source("good.json", "world_rpg:test/good", null)
        ));
        assertTrue(good.published());

        var lastKnownGood = publisher.active();

        ContentLoadResult bad = loader.loadAndPublish(List.of(
                source("bad.json", "world_rpg:test/bad", "world_rpg:test/missing")
        ));

        assertFalse(bad.published());
        assertTrue(bad.report().hasErrors());
        assertSame(lastKnownGood, publisher.active());
        assertEquals(1, publisher.active().require(FIXTURES).size());
    }

    @Test
    void unknownRegistryAndUnsupportedSchemaRejectCandidate() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = loader(publisher);

        ContentLoadResult unknownRegistry = loader.loadAndPublish(List.of(
                ContentSource.of(
                        "unknown.json",
                        """
                        {
                          "schema": 1,
                          "registry": "world_rpg:registry/nope",
                          "id": "world_rpg:test/nope"
                        }
                        """
                )
        ));
        assertFalse(unknownRegistry.published());
        assertTrue(unknownRegistry.report().hasErrors());

        ContentLoadResult newerSchema = loader.loadAndPublish(List.of(
                ContentSource.of(
                        "newer.json",
                        """
                        {
                          "schema": 2,
                          "registry": "world_rpg:registry/fixtures",
                          "id": "world_rpg:test/newer"
                        }
                        """
                )
        ));
        assertFalse(newerSchema.published());
        assertTrue(newerSchema.report().hasErrors());
    }

    private static ContentLoader loader(RegistryPublisher publisher) {
        DefinitionDomainHandler<FixtureDefinition> handler =
                new DefinitionDomainHandler<>(
                        DOMAIN,
                        ContentLoaderTransactionTest::decodeFixture,
                        (definition, candidate, source, report) ->
                                ReferenceResolver.resolve(
                                        definition.target(),
                                        candidate,
                                        report,
                                        source
                                ),
                        DefinitionValidator.none()
                );

        DefinitionDomainCatalog catalog = DefinitionDomainCatalog.builder()
                .add(handler)
                .addCrossRegistryValidator((candidate, report) -> {
                    if (candidate.registryCount() != 1) {
                        throw new AssertionError("fixture candidate should contain one registry");
                    }
                })
                .build();

        return new ContentLoader(catalog, publisher);
    }

    private static Optional<FixtureDefinition> decodeFixture(
            DecodedJsonDocument document,
            dev.worldrpg.api.validation.ValidationReport report
    ) {
        JsonElement targetElement = document.root().get("target");
        OptionalDefinitionRef<FixtureDefinition> target;

        if (targetElement == null || targetElement.isJsonNull()) {
            target = OptionalDefinitionRef.absent(FIXTURES);
        } else if (targetElement.isJsonPrimitive()
                && targetElement.getAsJsonPrimitive().isString()) {
            try {
                target = OptionalDefinitionRef.of(
                        FIXTURES,
                        RpgId.parse(targetElement.getAsString())
                );
            } catch (IllegalArgumentException exception) {
                report.error(
                        "fixture.target.invalid",
                        exception.getMessage(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }
        } else {
            report.error(
                    "fixture.target.type",
                    "target must be a namespaced string when present",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }

        return Optional.of(new FixtureDefinition(document.header().id(), target));
    }

    private static ContentSource source(
            String path,
            String id,
            String target
    ) {
        String targetJson = target == null
                ? ""
                : ",\n  \"target\": \"" + target + "\"";

        return ContentSource.of(
                path,
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/fixtures",
                  "id": "%s"%s
                }
                """.formatted(id, targetJson)
        );
    }

    private record FixtureDefinition(
            RpgId id,
            OptionalDefinitionRef<FixtureDefinition> target
    ) implements RpgDefinition {
    }
}
