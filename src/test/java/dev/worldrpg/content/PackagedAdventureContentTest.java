package dev.worldrpg.content;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.adventure.AdventureContentDomains;
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

class PackagedAdventureContentTest {
    private static final List<String> RESOURCES = List.of(
            "data/world_rpg/world_rpg/definitions/location/first_province_home.json",
            "data/world_rpg/world_rpg/definitions/location/east_road_checkpoint.json",
            "data/world_rpg/world_rpg/definitions/location/east_road_refuge.json",
            "data/world_rpg/world_rpg/definitions/location/collapsed_waystation.json",
            "data/world_rpg/world_rpg/definitions/npc/road_warden.json",
            "data/world_rpg/world_rpg/definitions/npc/refuge_scout.json",
            "data/world_rpg/world_rpg/definitions/item/road_worn_cloak.json",
            "data/world_rpg/world_rpg/definitions/item/roadside_provisions.json",
            "data/world_rpg/world_rpg/definitions/quest/east_road_disappearances.json",
            "data/world_rpg/world_rpg/definitions/quest/waystation_silence.json"
    );

    @Test
    void packagedAdventureSlicePublishesAsOneValidGraph()
            throws IOException {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        List<ContentSource> sources = RESOURCES.stream()
                .map(PackagedAdventureContentTest::source)
                .toList();

        var result = loader.loadAndPublish(sources);

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());

        var quests = publisher.active()
                .require(AdventureContentDomains.QUESTS);
        assertEquals(2, quests.size());

        var continuation = quests.require(RpgId.parse(
                "world_rpg:quest/first_province/waystation_silence"
        ));
        assertEquals(1, continuation.prerequisites().size());
        assertEquals(
                RpgId.parse(
                        "world_rpg:quest/first_province/east_road_disappearances"
                ),
                continuation.prerequisites().get(0).id()
        );
    }

    private static ContentSource source(String path) {
        try (InputStream input =
                     PackagedAdventureContentTest.class
                             .getClassLoader()
                             .getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException(
                        "missing packaged test resource: " + path
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
                    "failed reading packaged test resource: " + path,
                    exception
            );
        }
    }
}
