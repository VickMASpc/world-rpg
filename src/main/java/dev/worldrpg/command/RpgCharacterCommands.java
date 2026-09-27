package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.player.EquipmentSlot;
import dev.worldrpg.player.fabric.MinecraftCharacterRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

public final class RpgCharacterCommands {
    private static boolean registered;

    private RpgCharacterCommands() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            CommandManager.literal("rpgcharacter")
                                    .executes(context -> show(
                                            context.getSource()
                                                    .getPlayerOrThrow()
                                    ))
                                    .then(
                                            CommandManager.literal("equip")
                                                    .then(
                                                            CommandManager.argument(
                                                                    "item",
                                                                    RpgIdArgumentType.rpgId()
                                                            ).executes(context ->
                                                                    equip(
                                                                            context.getSource()
                                                                                    .getPlayerOrThrow(),
                                                                            RpgIdArgumentType.getRpgId(
                                                                                    context,
                                                                                    "item"
                                                                            )
                                                                    )
                                                            )
                                                    )
                                    )
                                    .then(
                                            CommandManager.literal("unequip")
                                                    .then(
                                                            CommandManager.argument(
                                                                    "slot",
                                                                    StringArgumentType.word()
                                                            ).executes(context ->
                                                                    unequip(
                                                                            context.getSource()
                                                                                    .getPlayerOrThrow(),
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "slot"
                                                                            )
                                                                    )
                                                            )
                                                    )
                                    )
                    );

                    dispatcher.register(
                            CommandManager.literal("worldrpg")
                                    .requires(source ->
                                            source.hasPermissionLevel(2)
                                    )
                                    .then(
                                            CommandManager.literal("character")
                                                    .then(
                                                            CommandManager.literal("grantxp")
                                                                    .then(
                                                                            CommandManager.argument(
                                                                                    "amount",
                                                                                    LongArgumentType.longArg(
                                                                                            0L,
                                                                                            1_000_000L
                                                                                    )
                                                                            ).executes(context ->
                                                                                    grantExperience(
                                                                                            context.getSource()
                                                                                                    .getPlayerOrThrow(),
                                                                                            LongArgumentType.getLong(
                                                                                                    context,
                                                                                                    "amount"
                                                                                            )
                                                                                    )
                                                                            )
                                                                    )
                                                    )
                                    )
                    );
                }
        );
    }

    private static int show(ServerPlayerEntity player) {
        var character = MinecraftCharacterRuntime.load(player);
        var combat =
                WorldRpgServerRuntime.productionCombat()
                        .characterStats(player);

        String xp = character.maxLevel()
                ? "MAX"
                : character.experienceIntoLevel()
                + "/"
                + character.experienceToNextLevel();

        player.sendMessage(
                Text.literal(
                        "RPG Character | Lv. "
                                + character.level()
                                + " | XP "
                                + xp
                                + " | HP "
                                + format(combat.maximumHealth())
                                + " | AP "
                                + format(combat.attackPower())
                                + " | Armor "
                                + format(combat.armor())
                ),
                false
        );

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            String label = slot.name()
                    .toLowerCase(Locale.ROOT)
                    .replace('_', ' ');
            String value = character.equipped(slot)
                    .flatMap(itemId ->
                            WorldRpgContentRuntime.publisher()
                                    .active()
                                    .require(
                                            AdventureContentDomains.ITEMS
                                    )
                                    .find(itemId)
                                    .map(item ->
                                            item.displayName()
                                                    + " ["
                                                    + itemId
                                                    + "]"
                                    )
                    )
                    .orElse("<empty>");

            player.sendMessage(
                    Text.literal(
                            "  "
                                    + label
                                    + ": "
                                    + value
                    ),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int equip(
            ServerPlayerEntity player,
            RpgId itemId
    ) {
        var result =
                MinecraftCharacterRuntime.equip(
                        player,
                        itemId
                );

        switch (result.status()) {
            case NOT_EQUIPMENT -> {
                player.sendMessage(
                        Text.literal(
                                result.item().displayName()
                                        + " is not equipment."
                        ),
                        false
                );
                return 0;
            }
            case LEVEL_TOO_LOW -> {
                player.sendMessage(
                        Text.literal(
                                result.item().displayName()
                                        + " requires level "
                                        + result.item().requiredLevel()
                                        + " (you are "
                                        + result.currentLevel()
                                        + ")."
                        ),
                        false
                );
                return 0;
            }
            case NOT_OWNED -> {
                player.sendMessage(
                        Text.literal(
                                "You do not own "
                                        + result.item().displayName()
                                        + "."
                        ),
                        false
                );
                return 0;
            }
            case EQUIPPED -> {
                WorldRpgServerRuntime.productionCombat()
                        .refreshPlayer(player);

                String replacement =
                        result.replacedItem() == null
                                ? ""
                                : " | replaced "
                                + result.replacedItem();

                player.sendMessage(
                        Text.literal(
                                "Equipped "
                                        + result.item().displayName()
                                        + " in "
                                        + result.slot()
                                        + replacement
                        ),
                        false
                );
                show(player);
                return Command.SINGLE_SUCCESS;
            }
        }

        return 0;
    }

    private static int unequip(
            ServerPlayerEntity player,
            String slotText
    ) {
        final EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(
                    slotText.trim()
                            .toUpperCase(Locale.ROOT)
                            .replace('-', '_')
            );
        } catch (IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal(
                            "Unknown equipment slot: "
                                    + slotText
                    ),
                    false
            );
            return 0;
        }

        var removed =
                MinecraftCharacterRuntime.unequip(
                        player,
                        slot
                );
        if (removed.isEmpty()) {
            player.sendMessage(
                    Text.literal(
                            "Nothing is equipped in "
                                    + slot
                                    + "."
                    ),
                    false
            );
            return 0;
        }

        WorldRpgServerRuntime.productionCombat()
                .refreshPlayer(player);

        player.sendMessage(
                Text.literal(
                        "Unequipped "
                                + removed.orElseThrow()
                                + " from "
                                + slot
                ),
                false
        );
        show(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int grantExperience(
            ServerPlayerEntity player,
            long amount
    ) {
        var result =
                MinecraftCharacterRuntime.grantExperience(
                        player,
                        amount
                );
        if (result.leveledUp()) {
            WorldRpgServerRuntime.productionCombat()
                    .refreshPlayer(player);
        }

        player.sendMessage(
                Text.literal(
                        "Granted "
                                + result.appliedExperience()
                                + " XP | level "
                                + result.startingLevel()
                                + " -> "
                                + result.endingLevel()
                ),
                false
        );
        show(player);
        return Command.SINGLE_SUCCESS;
    }

    private static String format(double value) {
        return String.format(
                Locale.ROOT,
                "%.1f",
                value
        );
    }
}
