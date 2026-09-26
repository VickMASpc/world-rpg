package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import dev.worldrpg.player.EquipmentSlot;
import dev.worldrpg.player.PlayerCharacterState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Optional;

public final class MinecraftCharacterRuntime {
    private static final String RPG_CHARACTER = "rpg_character";

    private MinecraftCharacterRuntime() {
    }

    public static PlayerCharacterState load(
            ServerPlayerEntity player
    ) {
        Objects.requireNonNull(player, "player");
        NbtCompound playerData =
                WorldRpgPersistentState.get(player.getServer())
                        .readPlayerData(player.getUuid());

        if (!playerData.contains(
                RPG_CHARACTER,
                NbtElement.COMPOUND_TYPE
        )) {
            return new PlayerCharacterState();
        }

        return PlayerCharacterStateNbtCodec.decode(
                playerData.getCompound(RPG_CHARACTER)
        );
    }

    public static PlayerCharacterState.ProgressionResult grantExperience(
            ServerPlayerEntity player,
            long amount
    ) {
        Objects.requireNonNull(player, "player");
        PlayerCharacterState state = load(player);
        var result = state.grantExperience(amount);
        save(player, state);
        return result;
    }

    public static EquipResult equip(
            ServerPlayerEntity player,
            RpgId itemId
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(itemId, "itemId");

        ItemContentDefinition item =
                WorldRpgContentRuntime.publisher()
                        .active()
                        .require(AdventureContentDomains.ITEMS)
                        .find(itemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "unknown RPG item: " + itemId
                                )
                        );

        if (item.category()
                != ItemContentDefinition.Category.EQUIPMENT) {
            return EquipResult.notEquipment(item);
        }

        PlayerCharacterState state = load(player);
        if (state.level() < item.requiredLevel()) {
            return EquipResult.levelTooLow(
                    item,
                    state.level()
            );
        }

        int quantity =
                MinecraftRpgInventoryRuntime.load(player)
                        .quantity(itemId);
        if (quantity < 1) {
            return EquipResult.notOwned(item);
        }

        var spec = item.equipmentSpec().orElseThrow();
        Optional<RpgId> replaced =
                state.equip(spec.slot(), itemId);
        save(player, state);

        return EquipResult.equipped(
                item,
                spec.slot(),
                replaced.orElse(null)
        );
    }

    public static Optional<RpgId> unequip(
            ServerPlayerEntity player,
            EquipmentSlot slot
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(slot, "slot");

        PlayerCharacterState state = load(player);
        Optional<RpgId> removed = state.unequip(slot);
        if (removed.isPresent()) {
            save(player, state);
        }
        return removed;
    }

    private static void save(
            ServerPlayerEntity player,
            PlayerCharacterState state
    ) {
        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound playerData =
                persistence.readPlayerData(player.getUuid());
        playerData.put(
                RPG_CHARACTER,
                PlayerCharacterStateNbtCodec.encode(state)
        );
        persistence.writePlayerData(
                player.getUuid(),
                playerData
        );
    }

    public record EquipResult(
            Status status,
            ItemContentDefinition item,
            EquipmentSlot slot,
            RpgId replacedItem,
            int currentLevel
    ) {
        public static EquipResult equipped(
                ItemContentDefinition item,
                EquipmentSlot slot,
                RpgId replaced
        ) {
            return new EquipResult(
                    Status.EQUIPPED,
                    item,
                    slot,
                    replaced,
                    -1
            );
        }

        public static EquipResult notEquipment(
                ItemContentDefinition item
        ) {
            return new EquipResult(
                    Status.NOT_EQUIPMENT,
                    item,
                    null,
                    null,
                    -1
            );
        }

        public static EquipResult levelTooLow(
                ItemContentDefinition item,
                int level
        ) {
            return new EquipResult(
                    Status.LEVEL_TOO_LOW,
                    item,
                    item.equipmentSpec()
                            .map(value -> value.slot())
                            .orElse(null),
                    null,
                    level
            );
        }

        public static EquipResult notOwned(
                ItemContentDefinition item
        ) {
            return new EquipResult(
                    Status.NOT_OWNED,
                    item,
                    item.equipmentSpec()
                            .map(value -> value.slot())
                            .orElse(null),
                    null,
                    -1
            );
        }
    }

    public enum Status {
        EQUIPPED,
        NOT_EQUIPMENT,
        LEVEL_TOO_LOW,
        NOT_OWNED
    }
}
