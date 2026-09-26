package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.NpcContentDefinition;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.quest.PlayerQuestLog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdventureQuestOfferPriorityTest {
    private static final RpgId NPC =
            RpgId.parse("world_rpg:npc/test/scout");

    private static final RpgId FIRST =
            RpgId.parse("world_rpg:quest/test/first");
    private static final RpgId SECOND =
            RpgId.parse("world_rpg:quest/test/second");
    private static final RpgId THIRD =
            RpgId.parse("world_rpg:quest/test/third");

    @Test
    void deeperLockedQuestScoresBehindNearestPrerequisite() {
        QuestContentDefinition first =
                quest(FIRST, List.of());
        QuestContentDefinition second =
                quest(
                        SECOND,
                        List.of(questRef(FIRST))
                );
        QuestContentDefinition third =
                quest(
                        THIRD,
                        List.of(questRef(SECOND))
                );

        List<QuestContentDefinition> definitions =
                List.of(first, second, third);
        PlayerQuestLog log = new PlayerQuestLog();

        assertEquals(
                1,
                AdventureQuestEventRouter
                        .unresolvedPrerequisiteCount(
                                second,
                                log,
                                definitions,
                                new HashSet<>()
                        )
        );
        assertEquals(
                2,
                AdventureQuestEventRouter
                        .unresolvedPrerequisiteCount(
                                third,
                                log,
                                definitions,
                                new HashSet<>()
                        )
        );

        log.accept(FIRST);
        log.markTurnedIn(FIRST);

        assertEquals(
                0,
                AdventureQuestEventRouter
                        .unresolvedPrerequisiteCount(
                                second,
                                log,
                                definitions,
                                new HashSet<>()
                        )
        );
        assertEquals(
                1,
                AdventureQuestEventRouter
                        .unresolvedPrerequisiteCount(
                                third,
                                log,
                                definitions,
                                new HashSet<>()
                        )
        );
    }

    private static QuestContentDefinition quest(
            RpgId id,
            List<RequiredDefinitionRef<QuestContentDefinition>>
                    prerequisites
    ) {
        RequiredDefinitionRef<NpcContentDefinition> npc =
                new RequiredDefinitionRef<>(
                        AdventureContentDomains.NPCS,
                        NPC
                );

        return new QuestContentDefinition(
                id,
                id.path(),
                "test quest",
                1,
                npc,
                npc,
                prerequisites,
                List.of(
                        new QuestObjectiveSpec.SpeakToNpc(
                                "report",
                                npc
                        )
                ),
                List.of(),
                0
        );
    }

    private static RequiredDefinitionRef<QuestContentDefinition>
    questRef(RpgId id) {
        return new RequiredDefinitionRef<>(
                AdventureContentDomains.QUESTS,
                id
        );
    }
}
