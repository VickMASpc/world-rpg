package dev.worldrpg.player;

import dev.worldrpg.api.id.RpgId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerCharacterStateTest {
    @Test
    void experienceAdvancesAcrossLevelThresholds() {
        PlayerCharacterState state =
                new PlayerCharacterState();

        long first =
                CharacterProgressionCurve.experienceToNextLevel(1);
        var almost = state.grantExperience(first - 1L);

        assertFalse(almost.leveledUp());
        assertEquals(1, state.level());
        assertEquals(first - 1L, state.experienceIntoLevel());

        var level = state.grantExperience(1L);

        assertTrue(level.leveledUp());
        assertEquals(2, state.level());
        assertEquals(0L, state.experienceIntoLevel());

        long second =
                CharacterProgressionCurve.experienceToNextLevel(2);
        state.grantExperience(second + 7L);

        assertEquals(3, state.level());
        assertEquals(7L, state.experienceIntoLevel());
    }

    @Test
    void equipmentIsSlotAuthoritativeAndReplacementIsExplicit() {
        PlayerCharacterState state =
                new PlayerCharacterState();
        RpgId cloak =
                RpgId.parse("world_rpg:item/test/cloak");
        RpgId mantle =
                RpgId.parse("world_rpg:item/test/mantle");

        assertTrue(
                state.equip(EquipmentSlot.BACK, cloak)
                        .isEmpty()
        );
        assertEquals(
                cloak,
                state.equipped(EquipmentSlot.BACK)
                        .orElseThrow()
        );

        assertEquals(
                cloak,
                state.equip(
                        EquipmentSlot.BACK,
                        mantle
                ).orElseThrow()
        );
        assertEquals(
                mantle,
                state.equipped(EquipmentSlot.BACK)
                        .orElseThrow()
        );

        assertEquals(
                mantle,
                state.unequip(EquipmentSlot.BACK)
                        .orElseThrow()
        );
        assertTrue(
                state.equipped(EquipmentSlot.BACK)
                        .isEmpty()
        );
    }
}
