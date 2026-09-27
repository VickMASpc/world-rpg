package dev.worldrpg.combat.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldRpgCoefficientDraftTest {
    @Test
    void directSpellScalesWithCastTimeUntilFullCoefficient() {
        assertEquals(
                0.5,
                WorldRpgCoefficientDraft.directSpell(35),
                1.0e-12
        );
        assertEquals(
                1.0,
                WorldRpgCoefficientDraft.directSpell(70),
                0.0
        );
        assertEquals(
                1.0,
                WorldRpgCoefficientDraft.directSpell(100),
                0.0
        );
    }

    @Test
    void instantDraftUsesOnePointFiveSecondEquivalent() {
        assertEquals(
                3.0 / 7.0,
                WorldRpgCoefficientDraft.instantSpell(),
                1.0e-12
        );
    }

    @Test
    void channelCoefficientIsDistributedAcrossTicks() {
        assertEquals(
                1.0,
                WorldRpgCoefficientDraft.channelTotal(80),
                0.0
        );
        assertEquals(
                0.25,
                WorldRpgCoefficientDraft.channelTick(80, 4),
                0.0
        );
    }

    @Test
    void invalidDurationsAndTickCountsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WorldRpgCoefficientDraft.directSpell(0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WorldRpgCoefficientDraft.channelTick(
                        60,
                        0
                )
        );
    }
}
