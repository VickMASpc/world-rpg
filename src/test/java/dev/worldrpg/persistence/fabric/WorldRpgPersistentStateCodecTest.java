package dev.worldrpg.persistence.fabric;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRpgPersistentStateCodecTest {
    @Test
    void worldAndPlayerPayloadsSurvivePersistentStateRoundTrip() {
        WorldRpgPersistentState state =
                new WorldRpgPersistentState();

        NbtCompound world = new NbtCompound();
        NbtCompound bindings = new NbtCompound();
        bindings.putString(
                "proof",
                "world-binding-survives"
        );
        world.put(
                "adventure_world_bindings",
                bindings
        );
        state.writeWorldData(world);

        UUID playerId = UUID.fromString(
                "11111111-2222-3333-4444-555555555555"
        );
        NbtCompound player = new NbtCompound();

        NbtCompound quest = new NbtCompound();
        quest.putString(
                "proof",
                "quest-survives"
        );
        player.put("quest_log", quest);

        NbtCompound inventory = new NbtCompound();
        inventory.putLong("copper", 95L);
        player.put("rpg_inventory", inventory);

        state.writePlayerData(
                playerId,
                player
        );

        NbtCompound encoded = state.writeNbt(
                new NbtCompound(),
                null
        );
        WorldRpgPersistentState restored =
                WorldRpgPersistentState.fromNbt(
                        encoded,
                        null
                );

        NbtCompound restoredWorld =
                restored.readWorldData();
        assertTrue(
                restoredWorld.contains(
                        "adventure_world_bindings"
                )
        );
        assertEquals(
                "world-binding-survives",
                restoredWorld
                        .getCompound(
                                "adventure_world_bindings"
                        )
                        .getString("proof")
        );

        NbtCompound restoredPlayer =
                restored.readPlayerData(playerId);
        assertEquals(
                "quest-survives",
                restoredPlayer
                        .getCompound("quest_log")
                        .getString("proof")
        );
        assertEquals(
                95L,
                restoredPlayer
                        .getCompound("rpg_inventory")
                        .getLong("copper")
        );
        assertEquals(1, restored.playerRecordCount());
    }
}
