package dev.worldrpg.network.combat;

import dev.worldrpg.WorldRpg;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record CombatStateRequestC2SPayload(
        UUID targetEntityUuid
) implements CustomPayload {
    public static final Id<CombatStateRequestC2SPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "combat/state_request_c2s"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            CombatStateRequestC2SPayload
            > CODEC = PacketCodec.of(
                    (payload, buf) -> {
                        boolean hasTarget =
                                payload.targetEntityUuid() != null;
                        buf.writeBoolean(hasTarget);
                        if (hasTarget) {
                            buf.writeUuid(
                                    payload.targetEntityUuid()
                            );
                        }
                    },
                    buf -> new CombatStateRequestC2SPayload(
                            buf.readBoolean()
                                    ? buf.readUuid()
                                    : null
                    )
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
