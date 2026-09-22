package dev.worldrpg.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionPaceProfileTest {
    @Test
    void currentDraftSitsInsideAcceptedJourneyBand() {
        ProgressionPaceProfile pace =
                WorldRpgProgressionDraft.FIRST_CHARACTER;

        assertEquals(100, pace.levelCap());

        double total = pace.journeyHours();
        assertTrue(total >= 350.0);
        assertTrue(total <= 500.0);

        assertEquals(
                429.25131776624096,
                total,
                1.0e-12
        );
    }

    @Test
    void localPaceRisesSmoothlyWithoutLateExponentialExplosion() {
        ProgressionPaceProfile pace =
                WorldRpgProgressionDraft.FIRST_CHARACTER;

        assertEquals(0.75, pace.hoursFromLevel(1), 0.0);
        assertTrue(
                pace.hoursFromLevel(10)
                        > pace.hoursFromLevel(1)
        );
        assertTrue(
                pace.hoursFromLevel(50)
                        > pace.hoursFromLevel(10)
        );
        assertEquals(6.5, pace.hoursFromLevel(99), 0.0);

        assertEquals(
                13.727008952997616,
                pace.cumulativeHoursToReach(10),
                1.0e-12
        );
        assertEquals(
                150.97151054552273,
                pace.cumulativeHoursToReach(50),
                1.0e-12
        );
    }

    @Test
    void levelBoundsAreExplicit() {
        ProgressionPaceProfile pace =
                WorldRpgProgressionDraft.FIRST_CHARACTER;

        assertThrows(
                IllegalArgumentException.class,
                () -> pace.hoursFromLevel(0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pace.hoursFromLevel(100)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pace.cumulativeHoursToReach(101)
        );
    }
}
