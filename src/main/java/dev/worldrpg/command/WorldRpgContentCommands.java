package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.DefinitionRegistry;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public final class WorldRpgContentCommands {
    private static boolean registered;

    private WorldRpgContentCommands() {
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
                                        .then(contentCommands())
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > contentCommands() {
        return CommandManager.literal("content")
                .then(
                        CommandManager.literal("status")
                                .executes(context -> status(context.getSource()))
                )
                .then(
                        CommandManager.literal("list")
                                .executes(context -> list(context.getSource()))
                )
                .then(
                        CommandManager.literal("inspect")
                                .then(
                                        CommandManager.argument(
                                                "registry",
                                                StringArgumentType.word()
                                        ).then(
                                                CommandManager.argument(
                                                        "id",
                                                        StringArgumentType.word()
                                                ).executes(context ->
                                                        inspect(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "registry"
                                                                ),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "id"
                                                                )
                                                        )
                                                )
                                        )
                                )
                );
    }

    private static int status(
            net.minecraft.server.command.ServerCommandSource source
    ) {
        var snapshot = WorldRpgContentRuntime.publisher().active();
        int definitions = snapshot.registries()
                .values()
                .stream()
                .mapToInt(DefinitionRegistry::size)
                .sum();

        String last = WorldRpgContentRuntime.lastResult()
                .map(result ->
                        " lastPublished=" + result.published()
                                + " errors=" + result.report().errorCount()
                                + " warnings=" + result.report().warningCount()
                )
                .orElse(" lastReload=<none>");

        source.sendFeedback(
                () -> Text.literal(
                        "World RPG content | registries="
                                + snapshot.registryCount()
                                + " definitions="
                                + definitions
                                + last
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int list(
            net.minecraft.server.command.ServerCommandSource source
    ) {
        var registries =
                WorldRpgContentRuntime.publisher().active().registries();

        if (registries.isEmpty()) {
            source.sendFeedback(
                    () -> Text.literal(
                            "World RPG content: no active definition registries."
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        registries.forEach((id, registry) ->
                source.sendFeedback(
                        () -> Text.literal(
                                id + " | definitions=" + registry.size()
                        ),
                        false
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int inspect(
            net.minecraft.server.command.ServerCommandSource source,
            String registryText,
            String definitionText
    ) {
        final RpgId registryId;
        final RpgId definitionId;

        try {
            registryId = RpgId.parse(registryText);
            definitionId = RpgId.parse(definitionText);
        } catch (IllegalArgumentException exception) {
            source.sendError(
                    Text.literal(exception.getMessage())
            );
            return 0;
        }

        DefinitionRegistry<?> registry =
                WorldRpgContentRuntime.publisher()
                        .active()
                        .registries()
                        .get(registryId);

        if (registry == null) {
            source.sendError(
                    Text.literal("Unknown registry: " + registryId)
            );
            return 0;
        }

        Object definition = registry.asMap().get(definitionId);
        if (definition == null) {
            source.sendError(
                    Text.literal(
                            "Unknown definition "
                                    + definitionId
                                    + " in "
                                    + registryId
                    )
            );
            return 0;
        }

        String sourceRef = registry.sourceOf(definitionId)
                .map(value -> value.display())
                .orElse("<unknown>");

        source.sendFeedback(
                () -> Text.literal(
                        registryId
                                + " / "
                                + definitionId
                                + " | source="
                                + sourceRef
                                + " | "
                                + definition
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }
}
