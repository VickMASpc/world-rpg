package dev.worldrpg.content;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.load.ContentLoader;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgContentDomainsTest {
    @Test
    void coherentAdventurePackagePublishesAsOneSnapshot() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                homeLocation(),
                checkpointLocation(),
                npc(),
                item("world_rpg:item/first_province/road_worn_cloak"),
                quest(
                        "world_rpg:location/first_province/east_road_checkpoint",
                        "world_rpg:item/first_province/road_worn_cloak"
                )
        ));

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());
        assertEquals(9, publisher.active().registryCount());
        assertTrue(
                publisher.active()
                        .require(AdventureContentDomains.QUESTS)
                        .find(RpgId.parse(
                                "world_rpg:quest/first_province/east_road_disappearances"
                        ))
                        .isPresent()
        );
    }

    @Test
    void missingQuestRewardRejectsWholeCandidate() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                homeLocation(),
                checkpointLocation(),
                npc(),
                quest(
                        "world_rpg:location/first_province/east_road_checkpoint",
                        "world_rpg:item/first_province/missing"
                )
        ));

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertEquals(0, publisher.active().registryCount());
    }

    @Test
    void missingPhysicalVisitLocationRejectsWholeCandidate() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                homeLocation(),
                npc(),
                item("world_rpg:item/first_province/road_worn_cloak"),
                quest(
                        "world_rpg:location/first_province/missing_checkpoint",
                        "world_rpg:item/first_province/road_worn_cloak"
                )
        ));

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertEquals(0, publisher.active().registryCount());
    }

    @Test
    void missingQuestPrerequisiteRejectsWholeCandidate() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                homeLocation(),
                checkpointLocation(),
                npc(),
                questWithPrerequisite(
                        "world_rpg:quest/first_province/missing_prerequisite"
                )
        ));

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertEquals(0, publisher.active().registryCount());
    }

    private static ContentSource homeLocation() {
        return ContentSource.of(
                "home_location.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/world_locations",
                  "id": "world_rpg:location/first_province/home",
                  "display_name": "Home Settlement (A)",
                  "kind": "settlement",
                  "tags": ["home", "services", "first_province"]
                }
                """
        );
    }

    private static ContentSource checkpointLocation() {
        return ContentSource.of(
                "checkpoint_location.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/world_locations",
                  "id": "world_rpg:location/first_province/east_road_checkpoint",
                  "display_name": "East Road Abandoned Checkpoint",
                  "kind": "road_checkpoint",
                  "tags": ["east_road", "investigation", "first_province"]
                }
                """
        );
    }

    private static ContentSource npc() {
        return ContentSource.of(
                "npc.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/npcs",
                  "id": "world_rpg:npc/first_province/road_warden",
                  "display_name": "Road Warden",
                  "home_location": "world_rpg:location/first_province/home",
                  "roles": ["quest_giver", "road_information"]
                }
                """
        );
    }

    private static ContentSource item(String id) {
        return ContentSource.of(
                "item.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/items",
                  "id": "%s",
                  "display_name": "Road-Worn Cloak",
                  "category": "equipment",
                  "required_level": 1,
                  "vendor_value_copper": 85,
                  "tags": ["cloth", "quest_reward", "first_province"]
                }
                """.formatted(id)
        );
    }

    private static ContentSource questWithPrerequisite(
            String prerequisiteId
    ) {
        return ContentSource.of(
                "prerequisite_quest.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/quests",
                  "id": "world_rpg:quest/first_province/east_road_disappearances",
                  "title": "Disappearances Along the East Road",
                  "journal_summary": "Prerequisite validation fixture.",
                  "minimum_level": 1,
                  "starter": "world_rpg:npc/first_province/road_warden",
                  "turn_in": "world_rpg:npc/first_province/road_warden",
                  "prerequisites": ["%s"],
                  "objectives": [
                    {
                      "key": "inspect_route",
                      "type": "visit_location",
                      "location": "world_rpg:location/first_province/east_road_checkpoint"
                    }
                  ],
                  "item_rewards": [],
                  "copper_reward": 0
                }
                """.formatted(prerequisiteId)
        );
    }

    private static ContentSource quest(
            String locationId,
            String rewardId
    ) {
        return ContentSource.of(
                "quest.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/quests",
                  "id": "world_rpg:quest/first_province/east_road_disappearances",
                  "title": "Disappearances Along the East Road",
                  "journal_summary": "The road warden has asked you to follow the east road, inspect the troubled route, and report back rather than treating the problem as a nearby kill errand.",
                  "minimum_level": 1,
                  "starter": "world_rpg:npc/first_province/road_warden",
                  "turn_in": "world_rpg:npc/first_province/road_warden",
                  "objectives": [
                    {
                      "key": "inspect_route",
                      "type": "visit_location",
                      "location": "%s"
                    },
                    {
                      "key": "report_to_warden",
                      "type": "speak_to_npc",
                      "npc": "world_rpg:npc/first_province/road_warden"
                    }
                  ],
                  "item_rewards": [
                    {"item": "%s", "quantity": 1}
                  ],
                  "copper_reward": 40
                }
                """.formatted(locationId, rewardId)
        );
    }
}
