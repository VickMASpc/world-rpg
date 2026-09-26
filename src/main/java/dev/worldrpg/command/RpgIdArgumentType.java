package dev.worldrpg.command;

import com.mojang.brigadier.context.CommandContext;
import dev.worldrpg.api.id.RpgId;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Shared command adapter for World RPG IDs.
 *
 * <p>RPG IDs use the same namespace:path lexical form as Minecraft
 * identifiers, including slash-delimited paths. Using Minecraft's built-in
 * identifier argument keeps command-tree serialization client-safe while
 * accepting values such as
 * {@code world_rpg:item/first_province/road_worn_cloak}.</p>
 */
public final class RpgIdArgumentType {
    private RpgIdArgumentType() {
    }

    public static IdentifierArgumentType rpgId() {
        return IdentifierArgumentType.identifier();
    }

    public static RpgId getRpgId(
            CommandContext<ServerCommandSource> context,
            String name
    ) {
        return RpgId.parse(
                IdentifierArgumentType.getIdentifier(
                        context,
                        name
                ).toString()
        );
    }
}
