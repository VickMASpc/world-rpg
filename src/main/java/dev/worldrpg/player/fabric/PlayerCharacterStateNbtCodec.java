package dev.worldrpg.player.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.player.EquipmentSlot;
import dev.worldrpg.player.PlayerCharacterState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.EnumMap;
import java.util.Map;

public final class PlayerCharacterStateNbtCodec {
    private static final String SCHEMA = "schema";
    private static final String LEVEL = "level";
    private static final String EXPERIENCE = "experience_into_level";
    private static final String EQUIPMENT = "equipment";
    private static final int CURRENT_SCHEMA = 1;

    private PlayerCharacterStateNbtCodec() {
    }

    public static NbtCompound encode(PlayerCharacterState state) {
        NbtCompound root = new NbtCompound();
        root.putInt(SCHEMA, CURRENT_SCHEMA);
        root.putInt(LEVEL, state.level());
        root.putLong(EXPERIENCE, state.experienceIntoLevel());

        NbtCompound equipment = new NbtCompound();
        state.equippedItems().forEach((slot, itemId) ->
                equipment.putString(
                        slot.name().toLowerCase(java.util.Locale.ROOT),
                        itemId.toString()
                )
        );
        root.put(EQUIPMENT, equipment);
        return root;
    }

    public static PlayerCharacterState decode(NbtCompound root) {
        int schema = root.getInt(SCHEMA);
        if (schema != CURRENT_SCHEMA) {
            throw new IllegalArgumentException(
                    "Unsupported RPG character schema: " + schema
            );
        }

        int level = root.getInt(LEVEL);
        long experience = root.getLong(EXPERIENCE);
        Map<EquipmentSlot, RpgId> equipment =
                new EnumMap<>(EquipmentSlot.class);

        if (root.contains(EQUIPMENT, NbtElement.COMPOUND_TYPE)) {
            NbtCompound equipmentNbt =
                    root.getCompound(EQUIPMENT);
            for (String slotName : equipmentNbt.getKeys()) {
                try {
                    EquipmentSlot slot = EquipmentSlot.valueOf(
                            slotName.toUpperCase(java.util.Locale.ROOT)
                    );
                    equipment.put(
                            slot,
                            RpgId.parse(
                                    equipmentNbt.getString(slotName)
                            )
                    );
                } catch (IllegalArgumentException ignored) {
                    // Unknown future/stale equipment slots do not block load.
                }
            }
        }

        return new PlayerCharacterState(
                level,
                experience,
                equipment
        );
    }
}
