package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.debug.P3ProofReport;
import dev.worldrpg.debug.P3ProofScenario;
import dev.worldrpg.integration.minecraft.MinecraftTargetProbe;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.integration.minecraft.p3.P3AbilityActivationRequest;
import dev.worldrpg.integration.minecraft.p3.P3FixtureDefinitions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public final class WorldRpgCommands {
    private WorldRpgCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        CommandManager.literal("worldrpg")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(p3Commands())
                )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > p3Commands() {
        return CommandManager.literal("p3")
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
                .then(roomCommands());
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > roomCommands() {
        return CommandManager.literal("room")
                .then(
                        CommandManager.literal("spawn")
                                .executes(context ->
                                        spawnRoomTarget(
                                                context.getSource().getPlayerOrThrow(),
                                                3
                                        )
                                )
                                .then(
                                        CommandManager.argument(
                                                "distance",
                                                IntegerArgumentType.integer(1, 30)
                                        ).executes(context ->
                                                spawnRoomTarget(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "distance"
                                                        )
                                                )
                                        )
                                )
                )
                .then(
                        CommandManager.literal("move")
                                .then(
                                        CommandManager.argument(
                                                "distance",
                                                IntegerArgumentType.integer(1, 30)
                                        ).executes(context ->
                                                moveRoomTarget(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "distance"
                                                        )
                                                )
                                        )
                                )
                )
                .then(
                        CommandManager.literal("health")
                                .then(
                                        CommandManager.argument(
                                                "value",
                                                IntegerArgumentType.integer(0, 100)
                                        ).executes(context ->
                                                setRoomTargetHealth(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "value"
                                                        )
                                                )
                                        )
                                )
                )
                .then(
                        CommandManager.literal("cast")
                                .then(
                                        CommandManager.literal("focus")
                                                .executes(context ->
                                                        activateRoomAbility(
                                                                context.getSource()
                                                                        .getPlayerOrThrow(),
                                                                P3FixtureDefinitions.FOCUS,
                                                                true
                                                        )
                                                )
                                )
                                .then(
                                        CommandManager.literal("bolt")
                                                .executes(context ->
                                                        activateRoomAbility(
                                                                context.getSource()
                                                                        .getPlayerOrThrow(),
                                                                P3FixtureDefinitions.BOLT,
                                                                false
                                                        )
                                                )
                                )
                                .then(
                                        CommandManager.literal("channel")
                                                .executes(context ->
                                                        activateRoomAbility(
                                                                context.getSource()
                                                                        .getPlayerOrThrow(),
                                                                P3FixtureDefinitions.CHANNEL,
                                                                false
                                                        )
                                                )
                                )
                )
                .then(
                        CommandManager.literal("status")
                                .executes(context -> {
                                    ServerPlayerEntity player =
                                            context.getSource()
                                                    .getPlayerOrThrow();

                                    var status =
                                            WorldRpgServerRuntime.p3Room()
                                                    .status(player);

                                    if (status.isEmpty()) {
                                        context.getSource().sendError(
                                                Text.literal(
                                                        "No P3 developer target is active."
                                                )
                                        );
                                        return 0;
                                    }

                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "P3 room | "
                                                            + status.orElseThrow()
                                            ),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        CommandManager.literal("reset")
                                .executes(context -> {
                                    ServerPlayerEntity player =
                                            context.getSource()
                                                    .getPlayerOrThrow();

                                    WorldRpgServerRuntime.p3Room()
                                            .reset(player);

                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "P3 developer room reset."
                                            ),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static int spawnRoomTarget(
            ServerPlayerEntity player,
            int distance
    ) {
        HuskEntity target =
                WorldRpgServerRuntime.p3Room()
                        .spawnTarget(player, distance);

        player.sendMessage(
                Text.literal(
                        "P3 developer target spawned "
                                + distance
                                + " blocks ahead | UUID="
                                + target.getUuid()
                                + " | F6 Focus, F7 Bolt, F8 Channel"
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int moveRoomTarget(
            ServerPlayerEntity player,
            int distance
    ) {
        try {
            LivingEntity target = WorldRpgServerRuntime.p3Room()
                    .moveTarget(player, distance);
            player.sendMessage(
                    Text.literal(
                            "P3 developer target moved to "
                                    + distance
                                    + " blocks ahead without resetting RPG state"
                                    + " | UUID=" + target.getUuid()
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal("P3 room move failed: " + exception.getMessage()),
                    false
            );
            return 0;
        }
    }

    private static int setRoomTargetHealth(
            ServerPlayerEntity player,
            int value
    ) {
        try {
            double applied = WorldRpgServerRuntime.p3Room()
                    .setTargetProofHealth(player, value);
            player.sendMessage(
                    Text.literal(
                            "P3 target proof-health set to " + applied
                                    + " (Minecraft entity health unchanged)"
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal("P3 room health failed: " + exception.getMessage()),
                    false
            );
            return 0;
        }
    }

    /**
     * Direct server-side developer activation. This intentionally bypasses the
     * C2S transport/replay layer and exists only to test server target
     * conditions that the temporary vanilla crosshair cannot submit (for
     * example, LOS-blocked or beyond-crosshair targets). F6/F7/F8 remain the
     * required evidence for the real network path.
     */
    private static int activateRoomAbility(
            ServerPlayerEntity player,
            RpgId abilityId,
            boolean selfTarget
    ) {
        try {
            UUID targetUuid = selfTarget
                    ? player.getUuid()
                    : WorldRpgServerRuntime.p3Room()
                            .targetUuid(player)
                            .orElseThrow(() -> new IllegalStateException(
                                    "No P3 developer target is active."
                            ));

            var response = WorldRpgServerRuntime.p3Combat().handle(
                    player,
                    new P3AbilityActivationRequest(
                            0L,
                            abilityId,
                            targetUuid
                    )
            );

            player.sendMessage(
                    Text.literal(
                            "P3 direct room activation "
                                    + abilityId + ": " + response.summary()
                                    + " (network path bypassed)"
                    ),
                    false
            );
            return response.accepted() ? Command.SINGLE_SUCCESS : 0;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal(
                            "P3 direct room activation failed: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }
    }
}
