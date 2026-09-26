package dev.worldrpg.content;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagedCombatExpeditionContentTest {
    private static final List<String> RESOURCES = List.of(
            "data/world_rpg/world_rpg/definitions/location/first_province_home.json",
            "data/world_rpg/world_rpg/definitions/location/east_road_checkpoint.json",
            "data/world_rpg/world_rpg/definitions/location/east_road_refuge.json",
            "data/world_rpg/world_rpg/definitions/location/collapsed_waystation.json",
            "data/world_rpg/world_rpg/definitions/location/ashwood_hunting_ground.json",
            "data/world_rpg/world_rpg/definitions/npc/road_warden.json",
            "data/world_rpg/world_rpg/definitions/npc/refuge_scout.json",
            "data/world_rpg/world_rpg/definitions/item/road_worn_cloak.json",
            "data/world_rpg/world_rpg/definitions/item/roadside_provisions.json",
            "data/world_rpg/world_rpg/definitions/item/ashwood_fang.json",
            "data/world_rpg/world_rpg/definitions/item/ashwood_pelt.json",
            "data/world_rpg/world_rpg/definitions/aura/ashwood_pack_fury.json",
            "data/world_rpg/world_rpg/definitions/ability/field_strike.json",
            "data/world_rpg/world_rpg/definitions/ability/ashwood_bite.json",
            "data/world_rpg/world_rpg/definitions/ability/ashwood_howl.json",
            "data/world_rpg/world_rpg/definitions/loot/ashwood_wolf.json",
            "data/world_rpg/world_rpg/definitions/loot/ashwood_stalker.json",
            "data/world_rpg/world_rpg/definitions/mob/ashwood_wolf.json",
            "data/world_rpg/world_rpg/definitions/mob/ashwood_stalker.json",
            "data/world_rpg/world_rpg/definitions/spawn_group/ashwood_wolves.json",
            "data/world_rpg/world_rpg/definitions/spawn_group/ashwood_stalker.json",
            "data/world_rpg/world_rpg/definitions/quest/east_road_disappearances.json",
            "data/world_rpg/world_rpg/definitions/quest/waystation_silence.json",
            "data/world_rpg/world_rpg/definitions/quest/ashwood_pressure.json"
    );

    @Test
    void packagedCombatExpeditionPublishesAsOneGraph() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                WorldRpgContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(
                RESOURCES.stream()
                        .map(PackagedCombatExpeditionContentTest::source)
                        .toList()
        );

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());

        var quests = publisher.active()
                .require(AdventureContentDomains.QUESTS);
        var mobs = publisher.active()
                .require(EnemyContentDomains.MOBS);
        var spawnGroups = publisher.active()
                .require(EnemyContentDomains.SPAWN_GROUPS);

        assertEquals(3, quests.size());
        assertEquals(2, mobs.size());
        assertEquals(2, spawnGroups.size());

        var hunt = quests.require(RpgId.parse(
                "world_rpg:quest/first_province/ashwood_pressure"
        ));
        assertEquals(3, hunt.objectives().size());

        QuestObjectiveSpec.DefeatMob defeat =
                assertInstanceOf(
                        QuestObjectiveSpec.DefeatMob.class,
                        hunt.objectives().get(1)
                );
        assertEquals(
                RpgId.parse(
                        "world_rpg:mob/first_province/ashwood_stalker"
                ),
                defeat.mob().id()
        );

        var wolfLoot = publisher.active()
                .require(EnemyContentDomains.LOOT_TABLES)
                .require(RpgId.parse(
                        "world_rpg:loot/first_province/ashwood_wolf"
                ));
        assertTrue(
                wolfLoot.entries().stream().anyMatch(entry ->
                        entry.item().id().equals(RpgId.parse(
                                "world_rpg:item/first_province/ashwood_fang"
                        ))
                )
        );
        assertFalse(
                wolfLoot.entries().stream().anyMatch(entry ->
                        entry.item().id().toString().contains("/dev/")
                )
        );
    }

    private static ContentSource source(String path) {
        try (InputStream input =
                     PackagedCombatExpeditionContentTest.class
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
