package dev.worldrpg.persistence.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.persistence.PersistedDefinitionPointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedDefinitionPointerNbtCodecTest {
    @Test
    void stableIdsRoundTripThroughNbt() {
        PersistedDefinitionPointer pointer =
                new PersistedDefinitionPointer(
                        RpgId.parse("world_rpg:registry/abilities"),
                        RpgId.parse("world_rpg:ability/mage/frostbolt")
                );

        assertEquals(
                pointer,
                PersistedDefinitionPointerNbtCodec.decode(
                        PersistedDefinitionPointerNbtCodec.encode(pointer)
                )
        );
    }
}
