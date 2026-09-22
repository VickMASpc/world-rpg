package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.debug.P3ProofReport;
import dev.worldrpg.debug.P3ProofScenario;
import dev.worldrpg.integration.minecraft.MinecraftTargetProbe;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
                                                .then(
                                                        CommandManager.literal("probe")
                                                                .then(
                                                                        CommandManager.argument(
                                                                                "target",
                                                                                EntityArgumentType.entity()
                                                                        ).executes(context -> {
                                                                            Entity sourceEntity =
                                                                                    context.getSource()
                                                                                            .getEntityOrThrow();
                                                                            Entity targetEntity =
                                                                                    EntityArgumentType.getEntity(
                                                                                            context,
                                                                                            "target"
                                                                                    );

                                                                            if (!(sourceEntity instanceof LivingEntity source)) {
                                                                                context.getSource().sendError(
                                                                                        Text.literal(
                                                                                                "P3 probe source must be a living entity."
                                                                                        )
                                                                                );
                                                                                return 0;
                                                                            }

                                                                            if (!(targetEntity instanceof LivingEntity target)) {
                                                                                context.getSource().sendError(
                                                                                        Text.literal(
                                                                                                "P3 probe target must be a living entity."
                                                                                        )
                                                                                );
                                                                                return 0;
                                                                            }

                                                                            MinecraftTargetProbe probe =
                                                                                    MinecraftTargetProbe.capture(
                                                                                            source,
                                                                                            target,
                                                                                            WorldRpgServerRuntime.actors()
                                                                                    );

                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal(
                                                                                            "P3 target probe | "
                                                                                                    + probe.summary()
                                                                                    ),
                                                                                    false
                                                                            );

                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                                )
                                                )
                                )
                )
        );
    }
}
