package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;

public final class RpgBagCommands {
    private static boolean registered;

    private RpgBagCommands() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("rpgbag")
                                        .executes(context -> show(
                                                context.getSource()
                                                        .getPlayerOrThrow()
                                        ))
                        )
        );
    }

    private static int show(ServerPlayerEntity player) {
        var inventory = MinecraftQuestRuntime.loadInventory(
                player
        );

        player.sendMessage(
                Text.literal(
                        "RPG Bag | copper="
                                + inventory.copper()
                                + " stacks="
                                + inventory.distinctItemCount()
                                + " items="
                                + inventory.totalItemCount()
                ),
                false
        );

        if (inventory.distinctItemCount() == 0) {
            player.sendMessage(
                    Text.literal("Your RPG bag is empty."),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        inventory.itemQuantities()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(
                        entry -> entry.getKey().toString()
                ))
                .forEach(entry -> {
                    var definition =
                            WorldRpgContentRuntime.publisher()
                                    .active()
                                    .require(
                                            AdventureContentDomains.ITEMS
                                    )
                                    .find(entry.getKey());

                    if (definition.isEmpty()) {
                        player.sendMessage(
                                Text.literal(
                                        entry.getKey()
                                                + " x"
                                                + entry.getValue()
                                                + " | unresolved item"
                                ),
                                false
                        );
                        return;
                    }

                    ItemContentDefinition item =
                            definition.orElseThrow();
                    player.sendMessage(
                            Text.literal(
                                    item.displayName()
                                            + " x"
                                            + entry.getValue()
                                            + " | "
                                            + item.category()
                                            + " | requires level "
                                            + item.requiredLevel()
                                            + " | vendor "
                                            + item.vendorValueCopper()
                                            + " copper"
                            ),
                            false
                    );
                });

        return Command.SINGLE_SUCCESS;
    }
}
