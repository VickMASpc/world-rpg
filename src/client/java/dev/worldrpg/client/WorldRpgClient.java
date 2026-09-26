package dev.worldrpg.client;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.client.combat.WorldRpgCombatHud;
import dev.worldrpg.client.combat.WorldRpgCombatInput;
import dev.worldrpg.client.entity.AshwoodWolfRenderer;
import dev.worldrpg.client.network.WorldRpgClientNetworking;
import dev.worldrpg.client.p3.P3DeveloperInput;
import dev.worldrpg.entity.WorldRpgEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class WorldRpgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                WorldRpgEntities.ASHWOOD_WOLF,
                AshwoodWolfRenderer::new
        );

        WorldRpgClientNetworking.register();
        WorldRpgCombatInput.register();
        WorldRpgCombatHud.register();
        P3DeveloperInput.register();
        WorldRpg.LOGGER.info("World RPG client bootstrap initialized.");
    }
}
