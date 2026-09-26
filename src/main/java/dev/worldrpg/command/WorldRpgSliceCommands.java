package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WorldRpgSliceCommands {
    private static boolean registered;

    private WorldRpgSliceCommands() {
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
                                        .then(sliceCommands())
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > sliceCommands() {
        return CommandManager.literal("slice")
                .then(
                        CommandManager.literal("build")
                                .then(
                                        CommandManager.literal("confirm")
                                                .executes(context -> build(
                                                        context.getSource()
                                                                .getPlayerOrThrow()
                                                ))
                                )
                )
                .then(
                        CommandManager.literal("status")
                                .executes(context -> status(
                                        context.getSource()
                                                .getPlayerOrThrow()
                                ))
                )
                .then(
                        CommandManager.literal("reset")
                                .then(
                                        CommandManager.literal("confirm")
                                                .executes(context -> reset(
                                                        context.getSource()
                                                                .getPlayerOrThrow()
                                                ))
                                )
                );
    }

    private static int build(ServerPlayerEntity player) {
        try {
            var result = WorldRpgServerRuntime.playableSlice()
                    .build(player);
            player.sendMessage(
                    Text.literal(result.summary()),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal(
                            "First playable slice build failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }

    private static int status(ServerPlayerEntity player) {
        try {
            player.sendMessage(
                    Text.literal(
                            WorldRpgServerRuntime.playableSlice()
                                    .statusSummary(player)
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException exception) {
            player.sendMessage(
                    Text.literal(
                            "First playable slice status failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }

    private static int reset(ServerPlayerEntity player) {
        try {
            boolean removed = WorldRpgServerRuntime.playableSlice()
                    .reset();
            player.sendMessage(
                    Text.literal(
                            removed
                                    ? "First playable slice state reset. The Road Warden was removed; placed test blocks are intentionally left in the disposable world."
                                    : "No first playable slice state exists."
                    ),
                    false
            );
            return removed ? Command.SINGLE_SUCCESS : 0;
        } catch (IllegalStateException exception) {
            player.sendMessage(
                    Text.literal(
                            "First playable slice reset failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }
}
