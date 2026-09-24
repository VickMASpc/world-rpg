package dev.worldrpg.persistence.fabric;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgPersistentStateTest {
    @Test
    void worldAndPlayerPayloadsRoundTripWithoutSharingMutableNbt() {
        WorldRpgPersistentState state =
                new WorldRpgPersistentState();

        NbtCompound world = new NbtCompound();
        world.putString("flag", "bridge_repaired");
        state.writeWorldData(world);

        UUID player = UUID.randomUUID();
        NbtCompound playerData = new NbtCompound();
        playerData.putInt("level", 7);
        state.writePlayerData(player, playerData);

        NbtCompound encoded = state.writeNbt(
                new NbtCompound(),
                null
        );

        WorldRpgPersistentState decoded =
                WorldRpgPersistentState.fromNbt(encoded, null);

        assertEquals(
                "bridge_repaired",
                decoded.readWorldData().getString("flag")
        );
        assertEquals(
                7,
                decoded.readPlayerData(player).getInt("level")
        );

        NbtCompound leaked = decoded.readPlayerData(player);
        leaked.putInt("level", 99);

        assertEquals(
                7,
                decoded.readPlayerData(player).getInt("level")
        );
        assertTrue(decoded.isDirty() == false);
    }
}
