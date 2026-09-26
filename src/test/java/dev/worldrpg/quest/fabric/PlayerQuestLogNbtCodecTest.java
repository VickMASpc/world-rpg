package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.quest.PlayerQuestLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerQuestLogNbtCodecTest {
    @Test
    void activeAndCompletedQuestHistoryRoundTrip() {
        RpgId activeQuest = RpgId.parse(
                "world_rpg:quest/test/east_road"
        );
        RpgId completedQuest = RpgId.parse(
                "world_rpg:quest/test/old_business"
        );

        PlayerQuestLog original = new PlayerQuestLog();
        original.accept(activeQuest);
        original.completeObjective(activeQuest, "inspect_wagon");
        original.accept(completedQuest);
        original.completeObjective(completedQuest, "report_back");
        original.markTurnedIn(completedQuest);

        PlayerQuestLog decoded = PlayerQuestLogNbtCodec.decode(
                PlayerQuestLogNbtCodec.encode(original)
        );

        assertEquals(1, decoded.activeCount());
        assertEquals(1, decoded.completedCount());
        assertTrue(
                decoded.find(activeQuest)
                        .orElseThrow()
                        .isObjectiveComplete("inspect_wagon")
        );
        assertTrue(decoded.hasCompleted(completedQuest));
    }
}
