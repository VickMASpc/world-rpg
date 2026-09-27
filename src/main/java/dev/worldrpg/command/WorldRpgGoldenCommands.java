package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WorldRpgGoldenCommands {
    public static final RpgId ASHWOOD_WOLF = RpgId.parse(
            "world_rpg:dev/golden/mob/ashwood_wolf"
    );

    private static boolean registered;

    private WorldRpgGoldenCommands() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("worldrpg")
                                        .requires(source ->
                                                source.hasPermissionLevel(2)
                                        )
                                        .then(
                                                CommandManager.literal("golden")
                                                        .then(
                                                                CommandManager.literal("ashwood")
                                                                        .then(
                                                                                CommandManager.literal("spawn")
                                                                                        .executes(context ->
                                                                                                spawnAshwood(
                                                                                                        context.getSource()
                                                                                                                .getPlayerOrThrow()
                                                                                                )
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("status")
                                                                        .executes(context ->
                                                                                status(
                                                                                        context.getSource()
                                                                                                .getPlayerOrThrow()
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int spawnAshwood(
            ServerPlayerEntity player
    ) {
        try {
            var result =
                    WorldRpgServerRuntime.authoredMobs()
                            .spawn(
                                    player,
                                    ASHWOOD_WOLF,
                                    10
                            );
            player.sendMessage(
                    Text.literal(result.summary()),
                    false
            );
            player.sendMessage(
                    Text.literal(
                            "Golden package note: gameplay/loot are authored; custom Ashwood Wolf rendering is intentionally deferred to the next asset iteration."
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendMessage(
                    Text.literal(
                            "Golden Ashwood Wolf spawn failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }

    private static int status(
            ServerPlayerEntity player
    ) {
        try {
            player.sendMessage(
                    Text.literal(
                            WorldRpgServerRuntime.authoredMobs()
                                    .statusSummary()
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException exception) {
            player.sendMessage(
                    Text.literal(
                            "Golden mob status failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }
}
