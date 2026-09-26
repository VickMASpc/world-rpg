package dev.worldrpg.combat.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeededCombatRollSourceTest {
    @Test
    void sameSeedProducesIdenticalStableStream() {
        SeededCombatRollSource first =
                new SeededCombatRollSource(123456789L);
        SeededCombatRollSource second =
                new SeededCombatRollSource(123456789L);

        for (int i = 0; i < 32; i++) {
            assertEquals(
                    first.nextUnit(),
                    second.nextUnit(),
                    0.0
            );
        }

        assertEquals(32, first.rollsConsumed());
        assertEquals(32, second.rollsConsumed());
    }

    @Test
    void streamHasPinnedRegressionValues() {
        SeededCombatRollSource rolls =
                new SeededCombatRollSource(0L);

        assertEquals(
                0.8833108082136426,
                rolls.nextUnit(),
                0.0
        );
        assertEquals(
                0.43152799704850997,
                rolls.nextUnit(),
                0.0
        );
        assertEquals(
                0.026433771592597743,
                rolls.nextUnit(),
                0.0
        );
    }

    @Test
    void unitRollsStayHalfOpenAndDifferentSeedsDiverge() {
        SeededCombatRollSource a =
                new SeededCombatRollSource(1L);
        SeededCombatRollSource b =
                new SeededCombatRollSource(2L);

        double a0 = a.nextUnit();
        double b0 = b.nextUnit();

        assertTrue(a0 >= 0.0 && a0 < 1.0);
        assertTrue(b0 >= 0.0 && b0 < 1.0);
        assertNotEquals(a0, b0);
    }
}
