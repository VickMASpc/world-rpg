package dev.worldrpg.quest;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.NpcContentDefinition;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.content.adventure.WorldLocationContentDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestObjectiveSequenceTest {
    private static final RpgId QUEST = RpgId.parse(
            "world_rpg:quest/test/ordered"
    );
    private static final RpgId LOCATION = RpgId.parse(
            "world_rpg:location/test/road"
    );
    private static final RpgId NPC = RpgId.parse(
            "world_rpg:npc/test/warden"
    );

    @Test
    void laterSpeakObjectiveCannotBecomeCurrentEarly() {
        QuestContentDefinition definition = definition();
        QuestProgress progress = new QuestProgress(QUEST);

        var first = QuestObjectiveSequence.firstIncomplete(
                definition,
                progress
        ).orElseThrow();

        assertEquals("inspect_route", first.key());
        assertTrue(first instanceof QuestObjectiveSpec.VisitLocation);

        progress.completeObjective("inspect_route");

        var second = QuestObjectiveSequence.firstIncomplete(
                definition,
                progress
        ).orElseThrow();

        assertEquals("report", second.key());
        assertTrue(second instanceof QuestObjectiveSpec.SpeakToNpc);

        progress.completeObjective("report");

        assertTrue(
                QuestObjectiveSequence.firstIncomplete(
                        definition,
                        progress
                ).isEmpty()
        );
    }

    private static QuestContentDefinition definition() {
        return new QuestContentDefinition(
                QUEST,
                "Ordered quest",
                "Test ordered quest.",
                1,
                new RequiredDefinitionRef<>(
                        AdventureContentDomains.NPCS,
                        NPC
                ),
                new RequiredDefinitionRef<>(
                        AdventureContentDomains.NPCS,
                        NPC
                ),
                List.of(
                        new QuestObjectiveSpec.VisitLocation(
                                "inspect_route",
                                new RequiredDefinitionRef<>(
                                        AdventureContentDomains.WORLD_LOCATIONS,
                                        LOCATION
                                )
                        ),
                        new QuestObjectiveSpec.SpeakToNpc(
                                "report",
                                new RequiredDefinitionRef<>(
                                        AdventureContentDomains.NPCS,
                                        NPC
                                )
                        )
                ),
                List.of(),
                0
        );
    }
}
