package dev.worldrpg.persistence;

import net.minecraft.nbt.NbtCompound;

import java.util.List;

public final class WorldRpgSaveSchema {
    public static final SaveSchemaVersion CURRENT =
            new SaveSchemaVersion(1);

    public static final SaveMigrator<NbtCompound> MIGRATOR =
            new SaveMigrator<>(CURRENT, List.of());

    private WorldRpgSaveSchema() {
    }
}
