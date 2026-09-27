package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.player.PlayerRpgInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Objects;

public final class MinecraftRpgInventoryRuntime {
    private static final String RPG_INVENTORY = "rpg_inventory";

    private MinecraftRpgInventoryRuntime() {
    }

    public static PlayerRpgInventory load(
            ServerPlayerEntity player
    ) {
        Objects.requireNonNull(player, "player");
        NbtCompound playerData =
                WorldRpgPersistentState.get(player.getServer())
                        .readPlayerData(player.getUuid());
        return decode(playerData);
    }

    public static PlayerRpgInventory grant(
            ServerPlayerEntity player,
            Map<RpgId, Integer> items,
            long copper
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(items, "items");
        if (copper < 0) {
            throw new IllegalArgumentException(
                    "copper grant must be >= 0"
            );
        }

        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound playerData =
                persistence.readPlayerData(player.getUuid());
        PlayerRpgInventory inventory = decode(playerData);

        for (var entry : items.entrySet()) {
            inventory.grantItem(
                    entry.getKey(),
                    entry.getValue()
            );
        }
        inventory.addCopper(copper);

        playerData.put(
                RPG_INVENTORY,
                PlayerRpgInventoryNbtCodec.encode(inventory)
        );
        persistence.writePlayerData(
                player.getUuid(),
                playerData
        );
        return inventory;
    }

    private static PlayerRpgInventory decode(
            NbtCompound playerData
    ) {
        if (!playerData.contains(
                RPG_INVENTORY,
                NbtElement.COMPOUND_TYPE
        )) {
            return new PlayerRpgInventory();
        }
        return PlayerRpgInventoryNbtCodec.decode(
                playerData.getCompound(RPG_INVENTORY)
        );
    }
}
