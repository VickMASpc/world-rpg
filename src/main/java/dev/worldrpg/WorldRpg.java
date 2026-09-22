package dev.worldrpg;

import dev.worldrpg.command.WorldRpgCommands;
import dev.worldrpg.command.WorldRpgContentCommands;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
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
        WorldRpgContentRuntime.initialize(
                DefinitionDomainCatalog.builder().build()
        );
        WorldRpgServerRuntime.registerLifecycle();
        WorldRpgNetworking.registerCommon();
        WorldRpgContentCommands.register();
        WorldRpgCommands.register();

        LOGGER.info("World RPG runtime bootstrap initialized.");
    }
}
