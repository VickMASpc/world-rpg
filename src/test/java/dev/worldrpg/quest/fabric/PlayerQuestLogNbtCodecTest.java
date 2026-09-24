package dev.worldrpg.quest.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.quest.PlayerQuestLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerQuestLogNbtCodecTest {
    @Test
    void activeQuestAndStableObjectiveKeysRoundTrip() {
        RpgId quest = RpgId.parse(
                "world_rpg:quest/test/east_road"
        );

        PlayerQuestLog original = new PlayerQuestLog();
        original.accept(quest);
        original.completeObjective(quest, "inspect_wagon");
        original.completeObjective(quest, "speak_to_survivor");

        PlayerQuestLog decoded = PlayerQuestLogNbtCodec.decode(
                PlayerQuestLogNbtCodec.encode(original)
        );

        assertEquals(1, decoded.activeCount());
        assertTrue(
                decoded.find(quest)
                        .orElseThrow()
                        .isObjectiveComplete("inspect_wagon")
        );
        assertTrue(
                decoded.find(quest)
                        .orElseThrow()
                        .isObjectiveComplete("speak_to_survivor")
        );
    }
}
