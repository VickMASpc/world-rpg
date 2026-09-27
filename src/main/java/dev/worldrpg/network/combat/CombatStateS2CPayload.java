package dev.worldrpg.network.combat;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.integration.minecraft.combat.ProductionCombatRuntime;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record CombatStateS2CPayload(
        long serverTick,
        ActorState self,
        ActorState target,
        boolean accepted,
        String feedback
) implements CustomPayload {
    public static final Id<CombatStateS2CPayload> ID =
            new Id<>(Identifier.of(
                    WorldRpg.MOD_ID,
                    "combat/state_s2c"
            ));

    public static final PacketCodec<
            RegistryByteBuf,
            CombatStateS2CPayload
            > CODEC = PacketCodec.of(
                    CombatStateS2CPayload::write,
                    CombatStateS2CPayload::read
            );

    public CombatStateS2CPayload {
        Objects.requireNonNull(self, "self");
        feedback = Objects.requireNonNull(
                feedback,
                "feedback"
        );
    }

    public static ActorState actor(
            ProductionCombatRuntime.CombatSnapshot snapshot,
            String displayName
    ) {
        return new ActorState(
                snapshot.entityUuid(),
                Objects.requireNonNull(
                        displayName,
                        "displayName"
                ),
                snapshot.level(),
                snapshot.health(),
                snapshot.maximumHealth(),
                snapshot.focus(),
                snapshot.maximumFocus(),
                snapshot.activeAbilityId(),
                snapshot.castEndsAtTick()
        );
    }

    private static void write(
            CombatStateS2CPayload payload,
            RegistryByteBuf buf
    ) {
        buf.writeLong(payload.serverTick());
        writeActor(buf, payload.self());

        boolean hasTarget = payload.target() != null;
        buf.writeBoolean(hasTarget);
        if (hasTarget) {
            writeActor(buf, payload.target());
        }

        buf.writeBoolean(payload.accepted());
        buf.writeString(payload.feedback());
    }

    private static CombatStateS2CPayload read(
            RegistryByteBuf buf
    ) {
        long serverTick = buf.readLong();
        ActorState self = readActor(buf);
        ActorState target = buf.readBoolean()
                ? readActor(buf)
                : null;
        boolean accepted = buf.readBoolean();
        String feedback = buf.readString();
        return new CombatStateS2CPayload(
                serverTick,
                self,
                target,
                accepted,
                feedback
        );
    }

    private static void writeActor(
            RegistryByteBuf buf,
            ActorState state
    ) {
        buf.writeUuid(state.entityUuid());
        buf.writeString(state.displayName());
        buf.writeVarInt(state.level());
        buf.writeDouble(state.health());
        buf.writeDouble(state.maximumHealth());
        buf.writeDouble(state.focus());
        buf.writeDouble(state.maximumFocus());

        boolean hasCast = state.activeAbilityId() != null;
        buf.writeBoolean(hasCast);
        if (hasCast) {
            buf.writeString(
                    state.activeAbilityId().toString()
            );
            buf.writeLong(state.castEndsAtTick());
        }
    }

    private static ActorState readActor(
            RegistryByteBuf buf
    ) {
        UUID uuid = buf.readUuid();
        String name = buf.readString();
        int level = buf.readVarInt();
        double health = buf.readDouble();
        double maximumHealth = buf.readDouble();
        double focus = buf.readDouble();
        double maximumFocus = buf.readDouble();

        RpgId activeAbility = null;
        long castEndsAtTick = -1L;
        if (buf.readBoolean()) {
            activeAbility = RpgId.parse(
                    buf.readString()
            );
            castEndsAtTick = buf.readLong();
        }

        return new ActorState(
                uuid,
                name,
                level,
                health,
                maximumHealth,
                focus,
                maximumFocus,
                activeAbility,
                castEndsAtTick
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record ActorState(
            UUID entityUuid,
            String displayName,
            int level,
            double health,
            double maximumHealth,
            double focus,
            double maximumFocus,
            RpgId activeAbilityId,
            long castEndsAtTick
    ) {
        public ActorState {
            Objects.requireNonNull(
                    entityUuid,
                    "entityUuid"
            );
            Objects.requireNonNull(
                    displayName,
                    "displayName"
            );
        }
    }
}
