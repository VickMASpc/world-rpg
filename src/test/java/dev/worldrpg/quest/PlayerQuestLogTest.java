package dev.worldrpg.quest;

import dev.worldrpg.api.id.RpgId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerQuestLogTest {
    private static final RpgId QUEST =
            RpgId.parse("world_rpg:quest/test/long_road");

    @Test
    void acceptanceAndStableObjectiveProgressAreIdempotent() {
        PlayerQuestLog log = new PlayerQuestLog();

        assertTrue(log.accept(QUEST));
        assertFalse(log.accept(QUEST));
        assertTrue(log.completeObjective(QUEST, "find_wagon"));
        assertFalse(log.completeObjective(QUEST, "find_wagon"));
        assertTrue(
                log.find(QUEST)
                        .orElseThrow()
                        .isObjectiveComplete("find_wagon")
        );
    }

    @Test
    void progressCannotBeAddedBeforeQuestAcceptance() {
        PlayerQuestLog log = new PlayerQuestLog();

        assertThrows(
                IllegalStateException.class,
                () -> log.completeObjective(QUEST, "find_wagon")
        );
    }
}
