package dev.worldrpg.client.network;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.network.p3.P3AbilityActivateS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public final class WorldRpgClientNetworking {
    private static boolean registered;

    private WorldRpgClientNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(
                P3AbilityActivateS2CPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    if (context.client().player != null) {
                        context.client().player.sendMessage(
                                Text.literal(
                                        "P3 activation #" + payload.sequence()
                                                + ": " + payload.summary()
                                ),
                                false
                        );
                    }
                })
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) ->
                        P3ClientAbilitySender.resetSequence()
        );

        WorldRpg.LOGGER.info(
                "World RPG P3 client networking registered."
        );
    }
}
