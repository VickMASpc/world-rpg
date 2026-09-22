package dev.worldrpg.combat.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgDefenseDraftTest {
    @Test
    void sameLevelReferenceBudgetsHoldStableMitigationTargets() {
        for (int level : new int[]{1, 10, 25, 50, 75, 100}) {
            double scale =
                    WorldRpgDefenseDraft.MITIGATION_SCALE
                            .valueAt(level);

            assertEquals(
                    WorldRpgDefenseDraft.REFERENCE_ARMOR_MITIGATION,
                    WorldRpgDefenseDraft.mitigation(
                            WorldRpgDefenseDraft.referenceArmor(level),
                            scale
                    ),
                    1.0e-12
            );

            assertEquals(
                    WorldRpgDefenseDraft.REFERENCE_RESISTANCE_MITIGATION,
                    WorldRpgDefenseDraft.mitigation(
                            WorldRpgDefenseDraft.referenceResistance(level),
                            scale
                    ),
                    1.0e-12
            );
        }
    }

    @Test
    void higherLevelAttackerNaturallyCutsThroughOldReferenceArmor() {
        double level30Armor =
                WorldRpgDefenseDraft.referenceArmor(30);

        double versusLevel30 =
                WorldRpgDefenseDraft.mitigation(
                        level30Armor,
                        WorldRpgDefenseDraft.MITIGATION_SCALE
                                .valueAt(30)
                );

        double versusLevel60 =
                WorldRpgDefenseDraft.mitigation(
                        level30Armor,
                        WorldRpgDefenseDraft.MITIGATION_SCALE
                                .valueAt(60)
                );

        assertEquals(0.25, versusLevel30, 1.0e-12);
        assertTrue(versusLevel60 < versusLevel30);
        assertTrue(versusLevel60 > 0.0);
    }

    @Test
    void defenseBudgetsGrowWithoutExploding() {
        assertEquals(
                33.333333333333336,
                WorldRpgDefenseDraft.referenceArmor(1),
                1.0e-12
        );
        assertEquals(
                300.0,
                WorldRpgDefenseDraft.referenceArmor(100),
                1.0e-12
        );

        assertEquals(
                17.647058823529413,
                WorldRpgDefenseDraft.referenceResistance(1),
                1.0e-12
        );
        assertEquals(
                158.8235294117647,
                WorldRpgDefenseDraft.referenceResistance(100),
                1.0e-12
        );
    }
}
