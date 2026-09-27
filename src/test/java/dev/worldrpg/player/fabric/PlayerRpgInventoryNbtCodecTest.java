package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.player.PlayerRpgInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerRpgInventoryNbtCodecTest {
    @Test
    void currencyAndAbstractRpgItemsRoundTrip() {
        RpgId cloak = RpgId.parse(
                "world_rpg:item/first_province/road_worn_cloak"
        );

        PlayerRpgInventory original = new PlayerRpgInventory();
        original.addCopper(140);
        original.grantItem(cloak, 2);

        PlayerRpgInventory decoded =
                PlayerRpgInventoryNbtCodec.decode(
                        PlayerRpgInventoryNbtCodec.encode(original)
                );

        assertEquals(140, decoded.copper());
        assertEquals(2, decoded.quantity(cloak));
        assertEquals(1, decoded.distinctItemCount());
    }
}
