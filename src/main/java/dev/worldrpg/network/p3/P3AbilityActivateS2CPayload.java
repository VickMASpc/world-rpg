package dev.worldrpg.network.p3;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionFailure;
import dev.worldrpg.integration.minecraft.p3.P3AbilityActivationResponse;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record P3AbilityActivateS2CPayload(
        long sequence,
        boolean accepted,
        boolean castStarted,
        List<ConditionFailure> failures
) implements CustomPayload {
    public static final Id<P3AbilityActivateS2CPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "p3/ability_activate_s2c"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            P3AbilityActivateS2CPayload
            > CODEC = PacketCodec.of(
                    (payload, buf) -> {
                        buf.writeLong(payload.sequence());
                        buf.writeBoolean(payload.accepted());
                        buf.writeBoolean(payload.castStarted());
                        buf.writeVarInt(payload.failures().size());

                        for (ConditionFailure failure : payload.failures()) {
                            buf.writeString(failure.code().toString());
                            buf.writeString(failure.message());
                        }
                    },
                    buf -> {
                        long sequence = buf.readLong();
                        boolean accepted = buf.readBoolean();
                        boolean castStarted = buf.readBoolean();
                        int failureCount = buf.readVarInt();

                        if (failureCount < 0 || failureCount > 64) {
                            throw new IllegalArgumentException(
                                    "invalid P3 failure count: " + failureCount
                            );
                        }

                        List<ConditionFailure> failures =
                                new ArrayList<>(failureCount);

                        for (int i = 0; i < failureCount; i++) {
                            failures.add(new ConditionFailure(
                                    RpgId.parse(buf.readString()),
                                    buf.readString()
                            ));
                        }

                        return new P3AbilityActivateS2CPayload(
                                sequence,
                                accepted,
                                castStarted,
                                failures
                        );
                    }
            );

    public P3AbilityActivateS2CPayload {
        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "response sequence must be >= 0"
            );
        }
        failures = List.copyOf(
                Objects.requireNonNull(failures, "failures")
        );
    }

    public static P3AbilityActivateS2CPayload from(
            P3AbilityActivationResponse response
    ) {
        Objects.requireNonNull(response, "response");

        return new P3AbilityActivateS2CPayload(
                response.sequence(),
                response.accepted(),
                response.castStarted(),
                response.failures()
        );
    }

    public String summary() {
        if (accepted) {
            return castStarted
                    ? "accepted — cast started"
                    : "accepted — instant";
        }

        if (failures.isEmpty()) {
            return "rejected";
        }

        ConditionFailure first = failures.get(0);
        return "rejected — " + first.code() + ": " + first.message();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
