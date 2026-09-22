package dev.worldrpg.network.p3;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.integration.minecraft.p3.P3AbilityActivationRequest;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record P3AbilityActivateC2SPayload(
        long sequence,
        RpgId abilityId,
        UUID targetEntityUuid
) implements CustomPayload {
    public static final Id<P3AbilityActivateC2SPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "p3/ability_activate_c2s"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            P3AbilityActivateC2SPayload
            > CODEC = PacketCodec.of(
                    (payload, buf) -> {
                        buf.writeLong(payload.sequence());
                        buf.writeString(payload.abilityId().toString());
                        buf.writeUuid(payload.targetEntityUuid());
                    },
                    buf -> new P3AbilityActivateC2SPayload(
                            buf.readLong(),
                            RpgId.parse(buf.readString()),
                            buf.readUuid()
                    )
            );

    public P3AbilityActivateC2SPayload {
        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "request sequence must be >= 0"
            );
        }
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(targetEntityUuid, "targetEntityUuid");
    }

    public P3AbilityActivationRequest toRequest() {
        return new P3AbilityActivationRequest(
                sequence,
                abilityId,
                targetEntityUuid
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
