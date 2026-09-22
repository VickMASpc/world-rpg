package dev.worldrpg;

import dev.worldrpg.command.WorldRpgCommands;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.network.WorldRpgNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WorldRpg implements ModInitializer {
    public static final String MOD_ID = "world_rpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        WorldRpgServerRuntime.registerLifecycle();
        WorldRpgNetworking.registerCommon();
        WorldRpgCommands.register();
        LOGGER.info("World RPG runtime bootstrap initialized.");
    }
}
