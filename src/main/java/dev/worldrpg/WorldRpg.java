package dev.worldrpg;

import dev.worldrpg.command.WorldRpgCommands;
import dev.worldrpg.command.WorldRpgContentCommands;
import dev.worldrpg.command.WorldRpgPersistenceCommands;
import dev.worldrpg.command.WorldRpgInventoryCommands;
import dev.worldrpg.command.WorldRpgQuestCommands;
import dev.worldrpg.content.WorldRpgContentDomains;
import dev.worldrpg.content.combat.P3CombatContentRuntime;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
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
        P3CombatContentRuntime.register();
        WorldRpgContentRuntime.initialize(
                WorldRpgContentDomains.catalog()
        );

        WorldRpgServerRuntime.registerLifecycle();
        WorldRpgNetworking.registerCommon();
        WorldRpgContentCommands.register();
        WorldRpgPersistenceCommands.register();
        WorldRpgInventoryCommands.register();
        WorldRpgQuestCommands.register();
        WorldRpgCommands.register();

        LOGGER.info("World RPG runtime bootstrap initialized.");
    }
}
