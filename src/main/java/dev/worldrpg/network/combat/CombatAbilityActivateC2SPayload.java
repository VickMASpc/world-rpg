package dev.worldrpg.network.combat;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record CombatAbilityActivateC2SPayload(
        long sequence,
        RpgId abilityId,
        UUID targetEntityUuid
) implements CustomPayload {
    public static final Id<CombatAbilityActivateC2SPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "combat/ability_activate_c2s"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            CombatAbilityActivateC2SPayload
            > CODEC = PacketCodec.of(
                    (payload, buf) -> {
                        buf.writeLong(payload.sequence());
                        buf.writeString(
                                payload.abilityId().toString()
                        );
                        buf.writeUuid(
                                payload.targetEntityUuid()
                        );
                    },
                    buf -> new CombatAbilityActivateC2SPayload(
                            buf.readLong(),
                            RpgId.parse(buf.readString()),
                            buf.readUuid()
                    )
            );

    public CombatAbilityActivateC2SPayload {
        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "sequence must be >= 0"
            );
        }
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(
                targetEntityUuid,
                "targetEntityUuid"
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
