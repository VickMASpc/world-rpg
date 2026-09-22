package dev.worldrpg.client.network;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.network.p3.P3AbilityActivateC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Client intent sender. Contains no gameplay resolution.
 */
public final class P3ClientAbilitySender {
    private static final AtomicLong NEXT_SEQUENCE = new AtomicLong();

    private P3ClientAbilitySender() {
    }

    public static long send(
            RpgId abilityId,
            UUID targetEntityUuid
    ) {
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(targetEntityUuid, "targetEntityUuid");

        long sequence = NEXT_SEQUENCE.getAndIncrement();

        ClientPlayNetworking.send(
                new P3AbilityActivateC2SPayload(
                        sequence,
                        abilityId,
                        targetEntityUuid
                )
        );

        return sequence;
    }

    public static void resetSequence() {
        NEXT_SEQUENCE.set(0L);
    }
}
