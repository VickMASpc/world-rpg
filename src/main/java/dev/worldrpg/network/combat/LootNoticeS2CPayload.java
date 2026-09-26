package dev.worldrpg.network.combat;

import dev.worldrpg.WorldRpg;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record LootNoticeS2CPayload(
        String message
) implements CustomPayload {
    public static final Id<LootNoticeS2CPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "combat/loot_notice_s2c"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            LootNoticeS2CPayload
            > CODEC = PacketCodec.of(
                    (payload, buf) ->
                            buf.writeString(payload.message()),
                    buf -> new LootNoticeS2CPayload(
                            buf.readString()
                    )
            );

    public LootNoticeS2CPayload {
        message = Objects.requireNonNull(
                message,
                "message"
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
