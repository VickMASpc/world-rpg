package dev.worldrpg.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgMagnitudeDraftTest {
    @Test
    void referenceHealthGrowsAboutOneOrderOfMagnitudeAcrossJourney() {
        QuadraticLevelCurve health =
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH;

        assertEquals(100.0, health.valueAt(1), 0.0);
        assertEquals(478.525, health.valueAt(50), 1.0e-12);
        assertEquals(988.525, health.valueAt(100), 1.0e-12);

        assertTrue(health.growthMultiple() > 9.0);
        assertTrue(health.growthMultiple() < 11.0);
    }

    @Test
    void resourcesAndPowerRemainReadableAtLevelOneHundred() {
        QuadraticLevelCurve resource =
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE;
        QuadraticLevelCurve power =
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER;

        assertEquals(100.0, resource.valueAt(1), 0.0);
        assertEquals(495.01, resource.valueAt(100), 1.0e-12);

        assertEquals(10.0, power.valueAt(1), 0.0);
        assertEquals(
                69.2515,
                power.valueAt(100),
                1.0e-12
        );

        assertTrue(resource.growthMultiple() < 5.0);
        assertTrue(power.growthMultiple() < 7.0);
    }

    @Test
    void growthIsMonotonicWithoutExponentialJumps() {
        for (QuadraticLevelCurve curve : new QuadraticLevelCurve[]{
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH,
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE,
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
        }) {
            double previous = curve.valueAt(1);

            for (int level = 2; level <= 100; level++) {
                double current = curve.valueAt(level);
                assertTrue(current > previous);
                previous = current;
            }
        }
    }
}
