package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.player.fabric.MinecraftRpgInventoryRuntime;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WorldRpgInventoryCommands {
    private static boolean registered;

    private WorldRpgInventoryCommands() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("worldrpg")
                                        .requires(source ->
                                                source.hasPermissionLevel(2)
                                        )
                                        .then(inventoryCommands())
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > inventoryCommands() {
        return CommandManager.literal("inventory")
                .then(
                        CommandManager.literal("status")
                                .executes(context -> status(
                                        context.getSource()
                                                .getPlayerOrThrow()
                                ))
                )
                .then(
                        CommandManager.literal("inspect")
                                .then(
                                        CommandManager.argument(
                                                "item",
                                                RpgIdArgumentType.rpgId()
                                        ).executes(context -> inspect(
                                                context.getSource()
                                                        .getPlayerOrThrow(),
                                                RpgIdArgumentType.getRpgId(
                                                        context,
                                                        "item"
                                                )
                                        ))
                                )
                )
                .then(
                        CommandManager.literal("grant")
                                .then(
                                        CommandManager.argument(
                                                "item",
                                                RpgIdArgumentType.rpgId()
                                        ).then(
                                                CommandManager.argument(
                                                        "quantity",
                                                        IntegerArgumentType.integer(
                                                                1,
                                                                999
                                                        )
                                                ).executes(context -> grant(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        RpgIdArgumentType.getRpgId(
                                                                context,
                                                                "item"
                                                        ),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "quantity"
                                                        )
                                                ))
                                        )
                                )
                );
    }

    private static int status(ServerPlayerEntity player) {
        var inventory = MinecraftQuestRuntime.loadInventory(player);
        player.sendMessage(
                Text.literal(
                        "RPG inventory | copper="
                                + inventory.copper()
                                + " distinctItems="
                                + inventory.distinctItemCount()
                                + " totalItems="
                                + inventory.totalItemCount()
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int grant(
            ServerPlayerEntity player,
            RpgId itemId,
            int quantity
    ) {
        var item = WorldRpgContentRuntime.publisher()
                .active()
                .require(AdventureContentDomains.ITEMS)
                .find(itemId);

        if (item.isEmpty()) {
            player.sendMessage(
                    Text.literal(
                            "Unknown RPG item: " + itemId
                    ),
                    false
            );
            return 0;
        }

        MinecraftRpgInventoryRuntime.grant(
                player,
                java.util.Map.of(itemId, quantity),
                0L
        );

        player.sendMessage(
                Text.literal(
                        "Granted "
                                + item.orElseThrow().displayName()
                                + " x"
                                + quantity
                                + " to the RPG bag."
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int inspect(
            ServerPlayerEntity player,
            RpgId itemId
    ) {
        int quantity = MinecraftQuestRuntime
                .loadInventory(player)
                .quantity(itemId);

        player.sendMessage(
                Text.literal(itemId + " | quantity=" + quantity),
                false
        );
        return Command.SINGLE_SUCCESS;
    }
}
