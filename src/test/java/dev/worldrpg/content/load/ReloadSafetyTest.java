package dev.worldrpg.content.load;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadSafetyTest {
    @Test
    void restartDomainRejectsChangedLiveCandidate() {
        Fixture fixture = new Fixture(ReloadSafety.RESTART);
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = fixture.loader(publisher, false);

        assertTrue(loader.loadAndPublish(List.of(
                fixture.source("world_rpg:test/a")
        )).published());

        var active = publisher.active();

        var changed = loader.loadAndPublish(List.of(
                fixture.source("world_rpg:test/b")
        ));

        assertFalse(changed.published());
        assertTrue(changed.report().messages().stream()
                .anyMatch(message ->
                        message.code().equals("reload.restart_required")
                ));
        assertSame(active, publisher.active());
    }

    @Test
    void guardedDomainRequiresGuardAndGuardMayAllowChange() {
        Fixture fixture = new Fixture(ReloadSafety.GUARDED);

        RegistryPublisher blockedPublisher =
                new RegistryPublisher();
        ContentLoader blocked =
                fixture.loader(blockedPublisher, false);

        assertTrue(blocked.loadAndPublish(List.of(
                fixture.source("world_rpg:test/a")
        )).published());

        assertFalse(blocked.loadAndPublish(List.of(
                fixture.source("world_rpg:test/b")
        )).published());

        RegistryPublisher allowedPublisher =
                new RegistryPublisher();
        ContentLoader allowed =
                fixture.loader(allowedPublisher, true);

        assertTrue(allowed.loadAndPublish(List.of(
                fixture.source("world_rpg:test/a")
        )).published());
        assertTrue(allowed.loadAndPublish(List.of(
                fixture.source("world_rpg:test/b")
        )).published());
        assertEquals(
                1,
                allowedPublisher.active()
                        .require(fixture.key)
                        .size()
        );
    }

    @Test
    void safeDomainMayChangeLive() {
        Fixture fixture = new Fixture(ReloadSafety.SAFE);
        RegistryPublisher publisher =
                new RegistryPublisher();
        ContentLoader loader =
                fixture.loader(publisher, false);

        assertTrue(loader.loadAndPublish(List.of(
                fixture.source("world_rpg:test/a")
        )).published());

        assertTrue(loader.loadAndPublish(List.of(
                fixture.source("world_rpg:test/b")
        )).published());
    }

    private static final class Fixture {
        private final RegistryKey<Definition> key =
                new RegistryKey<>(
                        RpgId.parse("world_rpg:registry/reload_fixture"),
                        Definition.class
                );
        private final DefinitionDomain<Definition> domain;

        private Fixture(ReloadSafety safety) {
            domain = new DefinitionDomain<>(
                    key,
                    new SchemaVersion(1),
                    safety
            );
        }

        private ContentLoader loader(
                RegistryPublisher publisher,
                boolean withGuard
        ) {
            DefinitionDomainHandler<Definition> handler =
                    DefinitionDomainHandler.of(
                            domain,
                            (document, report) ->
                                    Optional.of(new Definition(
                                            document.header().id()
                                    ))
                    );

            DefinitionDomainCatalog.Builder builder =
                    DefinitionDomainCatalog.builder()
                            .add(handler);

            if (withGuard) {
                builder.addReloadGuard(
                        key.id(),
                        (active, candidate, report) -> {
                            // Fixture explicitly permits the replacement.
                        }
                );
            }

            return new ContentLoader(
                    builder.build(),
                    publisher
            );
        }

        private ContentSource source(String id) {
            return ContentSource.of(
                    id + ".json",
                    """
                    {
                      "schema": 1,
                      "registry": "world_rpg:registry/reload_fixture",
                      "id": "%s"
                    }
                    """.formatted(id)
            );
        }
    }

    private record Definition(RpgId id)
            implements RpgDefinition {
    }
}
