package dev.worldrpg.combat.cooldown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownBookTest {
    @Test
    void cooldownBecomesReadyAtExactReadyTick() {
        CooldownBook book = new CooldownBook();
        CooldownKey key = CooldownKey.of("world_rpg:cooldown/test");

        assertTrue(book.isReady(key, 100));

        assertEquals(140, book.start(key, 40, 100));
        assertFalse(book.isReady(key, 139));
        assertEquals(1, book.remainingTicks(key, 139));
        assertTrue(book.isReady(key, 140));
        assertEquals(0, book.remainingTicks(key, 140));
    }

    @Test
    void zeroDurationDoesNotLeaveStaleCooldown() {
        CooldownBook book = new CooldownBook();
        CooldownKey key = CooldownKey.of("world_rpg:cooldown/test");

        book.start(key, 40, 100);
        book.start(key, 0, 110);

        assertTrue(book.isReady(key, 110));
        assertTrue(book.activeAt(110).isEmpty());
    }
}
