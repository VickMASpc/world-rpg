package dev.worldrpg.client;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.client.network.WorldRpgClientNetworking;
import dev.worldrpg.client.p3.P3DeveloperInput;
import net.fabricmc.api.ClientModInitializer;

public final class WorldRpgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRpgClientNetworking.register();
        P3DeveloperInput.register();
        WorldRpg.LOGGER.info("World RPG client bootstrap initialized.");
    }
}
