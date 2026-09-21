package dev.worldrpg.client;

import dev.worldrpg.WorldRpg;
import net.fabricmc.api.ClientModInitializer;

public final class WorldRpgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRpg.LOGGER.info("World RPG client bootstrap initialized.");
    }
}
