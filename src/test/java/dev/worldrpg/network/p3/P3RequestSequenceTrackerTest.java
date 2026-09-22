package dev.worldrpg.network.p3;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class P3RequestSequenceTrackerTest {
    @Test
    void sequenceMustIncreasePerPlayer() {
        P3RequestSequenceTracker tracker =
                new P3RequestSequenceTracker();
        UUID player = UUID.randomUUID();

        assertTrue(tracker.accept(player, 0));
        assertTrue(tracker.accept(player, 1));
        assertFalse(tracker.accept(player, 1));
        assertFalse(tracker.accept(player, 0));
        assertTrue(tracker.accept(player, 5));
        assertFalse(tracker.accept(player, 4));
    }

    @Test
    void playersHaveIndependentSequenceSpaces() {
        P3RequestSequenceTracker tracker =
                new P3RequestSequenceTracker();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertTrue(tracker.accept(first, 10));
        assertTrue(tracker.accept(second, 0));
        assertFalse(tracker.accept(first, 9));
        assertTrue(tracker.accept(second, 1));
    }

    @Test
    void disconnectRemovalAllowsFreshSequenceSpace() {
        P3RequestSequenceTracker tracker =
                new P3RequestSequenceTracker();
        UUID player = UUID.randomUUID();

        assertTrue(tracker.accept(player, 20));
        tracker.remove(player);
        assertTrue(tracker.accept(player, 0));
    }
}
