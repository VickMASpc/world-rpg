package dev.worldrpg.persistence.fabric;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.persistence.PersistedDefinitionPointer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Objects;

public final class PersistedDefinitionPointerNbtCodec {
    private static final String REGISTRY = "registry";
    private static final String DEFINITION = "definition";

    private PersistedDefinitionPointerNbtCodec() {
    }

    public static NbtCompound encode(
            PersistedDefinitionPointer pointer
    ) {
        Objects.requireNonNull(pointer, "pointer");

        NbtCompound nbt = new NbtCompound();
        nbt.putString(REGISTRY, pointer.registryId().toString());
        nbt.putString(DEFINITION, pointer.definitionId().toString());
        return nbt;
    }

    public static PersistedDefinitionPointer decode(
            NbtCompound nbt
    ) {
        Objects.requireNonNull(nbt, "nbt");

        if (!nbt.contains(REGISTRY, NbtElement.STRING_TYPE)
                || !nbt.contains(DEFINITION, NbtElement.STRING_TYPE)) {
            throw new IllegalArgumentException(
                    "persisted definition pointer requires string registry and definition"
            );
        }

        return new PersistedDefinitionPointer(
                RpgId.parse(nbt.getString(REGISTRY)),
                RpgId.parse(nbt.getString(DEFINITION))
        );
    }
}
