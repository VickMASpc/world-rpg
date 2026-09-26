package dev.worldrpg.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.worldrpg.api.id.RpgId;

import java.util.Collection;
import java.util.List;

/**
 * Brigadier argument for World RPG IDs.
 *
 * <p>Vanilla Brigadier word/string arguments treat ':' and '/' as token
 * boundaries, but RPG IDs intentionally use values such as
 * {@code world_rpg:item/first_province/road_worn_cloak}. This argument reads
 * one complete whitespace-delimited token and validates it as an {@link RpgId}.</p>
 */
public final class RpgIdArgumentType implements ArgumentType<RpgId> {
    private static final DynamicCommandExceptionType INVALID_ID =
            new DynamicCommandExceptionType(
                    message -> new LiteralMessage(String.valueOf(message))
            );

    private RpgIdArgumentType() {
    }

    public static RpgIdArgumentType rpgId() {
        return new RpgIdArgumentType();
    }

    public static RpgId getRpgId(
            CommandContext<?> context,
            String name
    ) {
        return context.getArgument(name, RpgId.class);
    }

    @Override
    public RpgId parse(StringReader reader)
            throws CommandSyntaxException {
        int start = reader.getCursor();

        while (reader.canRead()
                && !Character.isWhitespace(reader.peek())) {
            reader.skip();
        }

        String token = reader.getString().substring(
                start,
                reader.getCursor()
        );

        try {
            return RpgId.parse(token);
        } catch (IllegalArgumentException exception) {
            reader.setCursor(start);
            throw INVALID_ID.createWithContext(
                    reader,
                    exception.getMessage()
            );
        }
    }

    @Override
    public Collection<String> getExamples() {
        return List.of(
                "world_rpg:quest/first_province/east_road_disappearances",
                "world_rpg:item/first_province/road_worn_cloak"
        );
    }
}
