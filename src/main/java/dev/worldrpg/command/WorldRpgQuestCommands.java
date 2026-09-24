package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WorldRpgQuestCommands {
    private static boolean registered;

    private WorldRpgQuestCommands() {
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
                                        .then(questCommands())
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > questCommands() {
        return CommandManager.literal("quest")
                .then(
                        CommandManager.literal("accept")
                                .then(
                                        CommandManager.argument(
                                                "quest",
                                                StringArgumentType.word()
                                        ).executes(context -> accept(
                                                context.getSource()
                                                        .getPlayerOrThrow(),
                                                StringArgumentType.getString(
                                                        context,
                                                        "quest"
                                                )
                                        ))
                                )
                )
                .then(
                        CommandManager.literal("advance")
                                .then(
                                        CommandManager.argument(
                                                "quest",
                                                StringArgumentType.word()
                                        ).then(
                                                CommandManager.argument(
                                                        "objective",
                                                        StringArgumentType.word()
                                                ).executes(context -> advance(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "quest"
                                                        ),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "objective"
                                                        )
                                                ))
                                        )
                                )
                )
                .then(
                        CommandManager.literal("status")
                                .then(
                                        CommandManager.argument(
                                                "quest",
                                                StringArgumentType.word()
                                        ).executes(context -> status(
                                                context.getSource()
                                                        .getPlayerOrThrow(),
                                                StringArgumentType.getString(
                                                        context,
                                                        "quest"
                                                )
                                        ))
                                )
                )
                .then(
                        CommandManager.literal("list")
                                .executes(context -> list(
                                        context.getSource()
                                                .getPlayerOrThrow()
                                ))
                );
    }

    private static int accept(
            ServerPlayerEntity player,
            String questText
    ) {
        RpgId questId = parseId(player, questText);
        if (questId == null) return 0;

        var result = MinecraftQuestRuntime.accept(player, questId);
        player.sendMessage(
                Text.literal(
                        "Quest " + questId + " | " + result
                ),
                false
        );
        return result == MinecraftQuestRuntime.AcceptResult.ACCEPTED
                ? Command.SINGLE_SUCCESS
                : 0;
    }

    private static int advance(
            ServerPlayerEntity player,
            String questText,
            String objectiveKey
    ) {
        RpgId questId = parseId(player, questText);
        if (questId == null) return 0;

        var result = MinecraftQuestRuntime.completeObjective(
                player,
                questId,
                objectiveKey
        );
        player.sendMessage(
                Text.literal(
                        "Quest " + questId
                                + " objective=" + objectiveKey
                                + " | " + result
                ),
                false
        );

        return result == MinecraftQuestRuntime.AdvanceResult.ADVANCED
                || result
                == MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN
                ? Command.SINGLE_SUCCESS
                : 0;
    }

    private static int status(
            ServerPlayerEntity player,
            String questText
    ) {
        RpgId questId = parseId(player, questText);
        if (questId == null) return 0;

        var view = MinecraftQuestRuntime.view(player, questId);
        player.sendMessage(
                Text.literal(
                        "Quest " + view.questId()
                                + " | " + view.state()
                                + " objectives="
                                + view.completedObjectives()
                                + "/"
                                + view.totalObjectives()
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int list(ServerPlayerEntity player) {
        var log = MinecraftQuestRuntime.load(player);

        if (log.activeCount() == 0) {
            player.sendMessage(
                    Text.literal("Quest log is empty."),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        for (RpgId questId : log.activeQuestIds()) {
            var view = MinecraftQuestRuntime.view(player, questId);
            player.sendMessage(
                    Text.literal(
                            questId
                                    + " | " + view.state()
                                    + " objectives="
                                    + view.completedObjectives()
                                    + "/"
                                    + view.totalObjectives()
                    ),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static RpgId parseId(
            ServerPlayerEntity player,
            String text
    ) {
        try {
            return RpgId.parse(text);
        } catch (IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal(exception.getMessage()),
                    false
            );
            return null;
        }
    }
}
