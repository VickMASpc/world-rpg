package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.player.EquipmentSlot;
import dev.worldrpg.player.PlayerCharacterState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerCharacterStateNbtCodecTest {
    @Test
    void roundTripPreservesProgressionAndEquipment() {
        PlayerCharacterState original =
                new PlayerCharacterState(
                        2,
                        37L,
                        Map.of(
                                EquipmentSlot.BACK,
                                RpgId.parse(
                                        "world_rpg:item/first_province/road_worn_cloak"
                                ),
                                EquipmentSlot.NECK,
                                RpgId.parse(
                                        "world_rpg:item/first_province/ashwood_carved_charm"
                                )
                        )
                );

        PlayerCharacterState decoded =
                PlayerCharacterStateNbtCodec.decode(
                        PlayerCharacterStateNbtCodec.encode(
                                original
                        )
                );

        assertEquals(2, decoded.level());
        assertEquals(37L, decoded.experienceIntoLevel());
        assertEquals(
                original.equippedItems(),
                decoded.equippedItems()
        );
    }
}
