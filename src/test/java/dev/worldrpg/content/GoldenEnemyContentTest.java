package dev.worldrpg.content;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.load.ContentLoader;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoldenEnemyContentTest {
    private static final List<String> RESOURCES = List.of(
            "data/world_rpg/world_rpg/definitions/item/golden_ashwood_fang.json",
            "data/world_rpg/world_rpg/definitions/loot/golden_ashwood_wolf.json",
            "data/world_rpg/world_rpg/definitions/mob/golden_ashwood_wolf.json"
    );

    @Test
    void packagedGoldenEnemyGraphPublishes()
            throws IOException {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(
                RESOURCES.stream()
                        .map(GoldenEnemyContentTest::source)
                        .toList()
        );

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());

        var mobs = publisher.active()
                .require(EnemyContentDomains.MOBS);
        var loot = publisher.active()
                .require(EnemyContentDomains.LOOT_TABLES);

        assertEquals(1, mobs.size());
        assertEquals(1, loot.size());

        var wolf = mobs.require(RpgId.parse(
                "world_rpg:dev/golden/mob/ashwood_wolf"
        ));
        assertEquals("Ashwood Wolf", wolf.displayName());
        assertEquals(
                RpgId.parse(
                        "world_rpg:dev/golden/loot/ashwood_wolf"
                ),
                wolf.lootTable().id()
        );
    }

    @Test
    void missingLootReferenceRejectsGoldenMob() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                source(
                        "data/world_rpg/world_rpg/definitions/mob/golden_ashwood_wolf.json"
                )
        ));

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertEquals(0, publisher.active().registryCount());
    }

    private static ContentSource source(String path) {
        try (InputStream input =
                     GoldenEnemyContentTest.class
                             .getClassLoader()
                             .getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException(
                        "missing packaged test resource: "
                                + path
                );
            }

            return ContentSource.of(
                    path,
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    )
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "failed reading packaged test resource: "
                            + path,
                    exception
            );
        }
    }
}
