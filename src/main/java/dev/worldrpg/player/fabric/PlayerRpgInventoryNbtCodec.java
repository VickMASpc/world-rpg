package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.player.PlayerRpgInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerRpgInventoryNbtCodec {
    private static final String SCHEMA = "schema";
    private static final String COPPER = "copper";
    private static final String ITEMS = "items";
    private static final int CURRENT_SCHEMA = 1;

    private PlayerRpgInventoryNbtCodec() {
    }

    public static NbtCompound encode(PlayerRpgInventory inventory) {
        NbtCompound root = new NbtCompound();
        root.putInt(SCHEMA, CURRENT_SCHEMA);
        root.putLong(COPPER, inventory.copper());

        NbtCompound items = new NbtCompound();
        inventory.itemQuantities().forEach((itemId, quantity) ->
                items.putInt(itemId.toString(), quantity)
        );
        root.put(ITEMS, items);
        return root;
    }

    public static PlayerRpgInventory decode(NbtCompound root) {
        int schema = root.getInt(SCHEMA);
        if (schema != CURRENT_SCHEMA) {
            throw new IllegalArgumentException(
                    "Unsupported RPG inventory schema: " + schema
            );
        }

        long copper = root.getLong(COPPER);
        Map<RpgId, Integer> items = new LinkedHashMap<>();

        if (root.contains(ITEMS, NbtElement.COMPOUND_TYPE)) {
            NbtCompound itemNbt = root.getCompound(ITEMS);
            for (String itemIdText : itemNbt.getKeys()) {
                items.put(
                        RpgId.parse(itemIdText),
                        itemNbt.getInt(itemIdText)
                );
            }
        }
        return new PlayerRpgInventory(items, copper);
    }
}
