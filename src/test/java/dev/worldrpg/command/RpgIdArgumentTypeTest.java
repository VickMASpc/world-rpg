package dev.worldrpg.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.worldrpg.api.id.RpgId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RpgIdArgumentTypeTest {
    @Test
    void parsesFullNamespacedPathToken() throws CommandSyntaxException {
        String text =
                "world_rpg:item/first_province/road_worn_cloak";
        StringReader reader = new StringReader(text);

        RpgId parsed = RpgIdArgumentType.rpgId().parse(reader);

        assertEquals(RpgId.parse(text), parsed);
        assertFalse(reader.canRead());
    }

    @Test
    void stopsAtWhitespaceForFollowingArguments()
            throws CommandSyntaxException {
        String id =
                "world_rpg:quest/first_province/east_road_disappearances";
        StringReader reader = new StringReader(id + " inspect_route");

        RpgId parsed = RpgIdArgumentType.rpgId().parse(reader);

        assertEquals(RpgId.parse(id), parsed);
        assertEquals(' ', reader.peek());
    }

    @Test
    void rejectsNamespaceLessDeveloperShorthand() {
        StringReader reader = new StringReader(
                "east_road_disappearances"
        );

        assertThrows(
                CommandSyntaxException.class,
                () -> RpgIdArgumentType.rpgId().parse(reader)
        );
    }
}
