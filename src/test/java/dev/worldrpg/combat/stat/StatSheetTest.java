package dev.worldrpg.combat.stat;

import dev.worldrpg.api.id.RpgId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatSheetTest {
    private static final StatKey POWER =
            StatKey.of("world_rpg:stat/test_power");

    @Test
    void modifiersApplyDeterministicallyByPriorityThenHandle() {
        StatSheet sheet = new StatSheet();
        sheet.setBase(POWER, 100.0);

        sheet.addModifier(
                POWER,
                source("world_rpg:test/multiplier", 1),
                StatModifierOperation.MULTIPLY,
                2.0,
                20
        );
        sheet.addModifier(
                POWER,
                source("world_rpg:test/addition", 1),
                StatModifierOperation.ADD,
                10.0,
                10
        );

        assertEquals(220.0, sheet.value(POWER));
    }

    @Test
    void removingModifierInvalidatesCachedValue() {
        StatSheet sheet = new StatSheet();
        sheet.setBase(POWER, 50.0);

        ModifierHandle handle = sheet.addModifier(
                POWER,
                source("world_rpg:test/item", 7),
                StatModifierOperation.ADD,
                25.0,
                0
        );

        assertEquals(75.0, sheet.value(POWER));
        assertTrue(sheet.removeModifier(handle));
        assertEquals(50.0, sheet.value(POWER));
        assertFalse(sheet.removeModifier(handle));
    }

    @Test
    void identicalDefinitionsCanBeRemovedByRuntimeInstance() {
        StatSheet sheet = new StatSheet();
        sheet.setBase(POWER, 10.0);

        RpgId definition = RpgId.parse("world_rpg:item/test_ring");
        ModifierSource firstRing = new ModifierSource(definition, 100);
        ModifierSource secondRing = new ModifierSource(definition, 101);

        sheet.addModifier(POWER, firstRing, StatModifierOperation.ADD, 5.0, 0);
        sheet.addModifier(POWER, secondRing, StatModifierOperation.ADD, 5.0, 0);

        assertEquals(20.0, sheet.value(POWER));
        assertEquals(1, sheet.removeModifiersFrom(firstRing));
        assertEquals(15.0, sheet.value(POWER));
        assertEquals(1, sheet.modifierCount());
    }

    private static ModifierSource source(String id, long instanceId) {
        return new ModifierSource(RpgId.parse(id), instanceId);
    }
}
