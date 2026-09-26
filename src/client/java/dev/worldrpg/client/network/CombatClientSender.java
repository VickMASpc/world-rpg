package dev.worldrpg.client.network;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.network.combat.CombatAbilityActivateC2SPayload;
import dev.worldrpg.network.combat.CombatStateRequestC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class CombatClientSender {
    private static final AtomicLong NEXT_SEQUENCE =
            new AtomicLong();

    private CombatClientSender() {
    }

    public static void activate(
            RpgId abilityId,
            UUID targetEntityUuid
    ) {
        ClientPlayNetworking.send(
                new CombatAbilityActivateC2SPayload(
                        NEXT_SEQUENCE.getAndIncrement(),
                        Objects.requireNonNull(
                                abilityId,
                                "abilityId"
                        ),
                        Objects.requireNonNull(
                                targetEntityUuid,
                                "targetEntityUuid"
                        )
                )
        );
    }

    public static void requestState(UUID targetEntityUuid) {
        ClientPlayNetworking.send(
                new CombatStateRequestC2SPayload(
                        targetEntityUuid
                )
        );
    }

    public static void reset() {
        NEXT_SEQUENCE.set(0L);
    }
}
