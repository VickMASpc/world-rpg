package dev.worldrpg.progression;

import dev.worldrpg.combat.math.WorldRpgDefenseDraft;
import dev.worldrpg.combat.math.WorldRpgRatingDraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgReferenceCurveInvariantTest {
    @Test
    void levelOneToHundredGrowthRemainsRestrained() {
        assertTrue(
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .growthMultiple() <= 10.0
        );
        assertTrue(
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE
                        .growthMultiple() <= 5.0
        );
        assertTrue(
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .growthMultiple() <= 7.0
        );

        assertTrue(
                WorldRpgDefenseDraft.referenceArmor(100)
                        / WorldRpgDefenseDraft.referenceArmor(1)
                        <= 9.0 + 1.0e-12
        );
        assertTrue(
                WorldRpgDefenseDraft.referenceResistance(100)
                        / WorldRpgDefenseDraft.referenceResistance(1)
                        <= 9.0 + 1.0e-12
        );

        assertTrue(
                WorldRpgRatingDraft.REFERENCE_RATING_PER_PERCENT
                        .ratingPerOnePercent(100)
                        / WorldRpgRatingDraft.REFERENCE_RATING_PER_PERCENT
                                .ratingPerOnePercent(1)
                        <= 8.0 + 1.0e-12
        );
    }

    @Test
    void referenceCheckpointsRemainInspectablyStable() {
        assertCheckpoint(
                1,
                100.0,
                100.0,
                10.0,
                33.333333333333336,
                17.647058823529413,
                1.0
        );
        assertCheckpoint(
                20,
                232.525,
                160.61,
                19.0915,
                104.53062236003002,
                55.33974124942766,
                3.0297233354890065
        );
        assertCheckpoint(
                50,
                478.525,
                271.01,
                35.6515,
                185.2542039706037,
                98.07575504326078,
                5.130652914010302
        );
        assertCheckpoint(
                100,
                988.525,
                495.01,
                69.25150000000001,
                300.0,
                158.8235294117647,
                8.0
        );
    }

    @Test
    void noSingleLevelReferenceJumpExplodes() {
        assertMaximumAdjacentGrowth(
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH,
                0.07
        );
        assertMaximumAdjacentGrowth(
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE,
                0.04
        );
        assertMaximumAdjacentGrowth(
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER,
                0.05
        );
    }

    private static void assertCheckpoint(
            int level,
            double health,
            double resource,
            double power,
            double armor,
            double resistance,
            double ratingPerPercent
    ) {
        assertEquals(
                health,
                WorldRpgMagnitudeDraft.REFERENCE_HEALTH
                        .valueAt(level),
                1.0e-12
        );
        assertEquals(
                resource,
                WorldRpgMagnitudeDraft.REFERENCE_PRIMARY_RESOURCE
                        .valueAt(level),
                1.0e-12
        );
        assertEquals(
                power,
                WorldRpgMagnitudeDraft.REFERENCE_BASE_POWER
                        .valueAt(level),
                1.0e-12
        );
        assertEquals(
                armor,
                WorldRpgDefenseDraft.referenceArmor(level),
                1.0e-12
        );
        assertEquals(
                resistance,
                WorldRpgDefenseDraft.referenceResistance(level),
                1.0e-12
        );
        assertEquals(
                ratingPerPercent,
                WorldRpgRatingDraft.REFERENCE_RATING_PER_PERCENT
                        .ratingPerOnePercent(level),
                1.0e-12
        );
    }

    private static void assertMaximumAdjacentGrowth(
            QuadraticLevelCurve curve,
            double maximumFraction
    ) {
        for (int level = 2; level <= curve.levelCap(); level++) {
            double previous = curve.valueAt(level - 1);
            double current = curve.valueAt(level);
            double growth = current / previous - 1.0;

            assertTrue(
                    growth <= maximumFraction,
                    "level " + level
                            + " grew by "
                            + growth
                            + " > "
                            + maximumFraction
            );
        }
    }
}
