package dev.worldrpg.network;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionFailure;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.integration.minecraft.p3.P3AbilityActivationResponse;
import dev.worldrpg.network.p3.P3AbilityActivateC2SPayload;
import dev.worldrpg.network.p3.P3AbilityActivateS2CPayload;
import dev.worldrpg.network.p3.P3RequestSequenceTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.List;
import java.util.NoSuchElementException;

public final class WorldRpgNetworking {
    private static final RpgId STALE_REQUEST =
            RpgId.parse("world_rpg:condition/stale_request_sequence");
    private static final RpgId INVALID_REQUEST =
            RpgId.parse("world_rpg:condition/invalid_activation_request");

    private static final P3RequestSequenceTracker P3_SEQUENCES =
            new P3RequestSequenceTracker();

    private static boolean registered;

    private WorldRpgNetworking() {
    }

    public static void registerCommon() {
        if (registered) {
            return;
        }
        registered = true;

        PayloadTypeRegistry.playC2S().register(
                P3AbilityActivateC2SPayload.ID,
                P3AbilityActivateC2SPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                P3AbilityActivateS2CPayload.ID,
                P3AbilityActivateS2CPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                P3AbilityActivateC2SPayload.ID,
                (payload, context) -> context.server().execute(() -> {
                    P3AbilityActivationResponse response;

                    if (!P3_SEQUENCES.accept(
                            context.player().getUuid(),
                            payload.sequence()
                    )) {
                        response = rejected(
                                payload.sequence(),
                                STALE_REQUEST,
                                "Activation request sequence is stale or duplicated"
                        );
                    } else {
                        try {
                            response = WorldRpgServerRuntime.p3Combat().handle(
                                    context.player(),
                                    payload.toRequest()
                            );
                        } catch (NoSuchElementException
                                 | IllegalArgumentException exception) {
                            response = rejected(
                                    payload.sequence(),
                                    INVALID_REQUEST,
                                    exception.getMessage() == null
                                            ? "Invalid activation request"
                                            : exception.getMessage()
                            );
                        }
                    }

                    ServerPlayNetworking.send(
                            context.player(),
                            P3AbilityActivateS2CPayload.from(response)
                    );
                })
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> P3_SEQUENCES.remove(
                        handler.getPlayer().getUuid()
                )
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> P3_SEQUENCES.clear()
        );

        WorldRpg.LOGGER.info(
                "World RPG P3 activation networking registered."
        );
    }

    private static P3AbilityActivationResponse rejected(
            long sequence,
            RpgId code,
            String message
    ) {
        return new P3AbilityActivationResponse(
                sequence,
                false,
                List.of(new ConditionFailure(code, message)),
                false
        );
    }
}
