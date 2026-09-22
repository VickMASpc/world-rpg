package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.debug.P3ProofReport;
import dev.worldrpg.debug.P3ProofScenario;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public final class WorldRpgCommands {
    private WorldRpgCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        CommandManager.literal("worldrpg")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(
                                        CommandManager.literal("p3")
                                                .then(
                                                        CommandManager.literal("proof")
                                                                .executes(context -> {
                                                                    P3ProofReport report =
                                                                            P3ProofScenario.run();

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(
                                                                                    report.summary()
                                                                            ),
                                                                            false
                                                                    );

                                                                    return report.passed()
                                                                            ? Command.SINGLE_SUCCESS
                                                                            : 0;
                                                                })
                                                )
                                )
                )
        );
    }
}
