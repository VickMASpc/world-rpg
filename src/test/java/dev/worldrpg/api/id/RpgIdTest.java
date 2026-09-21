package dev.worldrpg.api.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RpgIdTest {
    @Test
    void parsesStableNamespacedId() {
        RpgId id = RpgId.parse("world_rpg:ability/mage/frostbolt");

        assertEquals("world_rpg", id.namespace());
        assertEquals("ability/mage/frostbolt", id.path());
        assertEquals("world_rpg:ability/mage/frostbolt", id.toString());
    }

    @Test
    void rejectsMissingNamespace() {
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("frostbolt"));
    }

    @Test
    void rejectsUppercaseAndWhitespace() {
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world_rpg:Ability/Frostbolt"));
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world rpg:ability/frostbolt"));
    }

    @Test
    void rejectsExtraSeparator() {
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world_rpg:ability:frostbolt"));
    }

    @Test
    void rejectsEmptyPathSegments() {
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world_rpg:/ability"));
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world_rpg:ability/"));
        assertThrows(IllegalArgumentException.class, () -> RpgId.parse("world_rpg:ability//frostbolt"));
    }
}
