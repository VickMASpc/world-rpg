package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.persistence.PersistedDefinitionPointer;
import dev.worldrpg.persistence.WorldRpgSaveSchema;
import dev.worldrpg.persistence.fabric.PersistedDefinitionPointerNbtCodec;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class WorldRpgPersistenceCommands {
    private static final String PROBE_REF = "developer_probe_ref";
    private static boolean registered;

    private WorldRpgPersistenceCommands() {
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
                                        .then(persistenceCommands())
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.minecraft.server.command.ServerCommandSource
            > persistenceCommands() {
        return CommandManager.literal("persistence")
                .then(
                        CommandManager.literal("status")
                                .executes(context -> status(context.getSource()))
                )
                .then(
                        CommandManager.literal("setref")
                                .then(
                                        CommandManager.argument(
                                                "registry",
                                                RpgIdArgumentType.rpgId()
                                        ).then(
                                                CommandManager.argument(
                                                        "definition",
                                                        RpgIdArgumentType.rpgId()
                                                ).executes(context -> setRef(
                                                        context.getSource()
                                                                .getPlayerOrThrow(),
                                                        RpgIdArgumentType.getRpgId(
                                                                context,
                                                                "registry"
                                                        ),
                                                        RpgIdArgumentType.getRpgId(
                                                                context,
                                                                "definition"
                                                        )
                                                ))
                                        )
                                )
                )
                .then(
                        CommandManager.literal("showref")
                                .executes(context -> showRef(
                                        context.getSource().getPlayerOrThrow()
                                ))
                )
                .then(
                        CommandManager.literal("clearref")
                                .executes(context -> clearRef(
                                        context.getSource().getPlayerOrThrow()
                                ))
                );
    }

    private static int status(
            net.minecraft.server.command.ServerCommandSource source
    ) {
        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(source.getServer());

        source.sendFeedback(
                () -> Text.literal(
                        "World RPG persistence | schema="
                                + WorldRpgSaveSchema.CURRENT.value()
                                + " playerRecords="
                                + state.playerRecordCount()
                                + " worldKeys="
                                + state.worldKeyCount()
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int setRef(
            ServerPlayerEntity player,
            RpgId registryId,
            RpgId definitionId
    ) {
        PersistedDefinitionPointer pointer =
                new PersistedDefinitionPointer(
                        registryId,
                        definitionId
                );

        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(player.getServer());

        NbtCompound data =
                state.readPlayerData(player.getUuid());
        data.put(
                PROBE_REF,
                PersistedDefinitionPointerNbtCodec.encode(pointer)
        );
        state.writePlayerData(player.getUuid(), data);

        player.sendMessage(
                Text.literal(
                        "Persisted developer reference: "
                                + pointer.registryId()
                                + " -> "
                                + pointer.definitionId()
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int showRef(ServerPlayerEntity player) {
        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound data =
                state.readPlayerData(player.getUuid());

        if (!data.contains(PROBE_REF, NbtElement.COMPOUND_TYPE)) {
            player.sendMessage(
                    Text.literal("No persisted developer reference."),
                    false
            );
            return 0;
        }

        final PersistedDefinitionPointer pointer;
        try {
            pointer = PersistedDefinitionPointerNbtCodec.decode(
                    data.getCompound(PROBE_REF)
            );
        } catch (IllegalArgumentException exception) {
            player.sendMessage(
                    Text.literal(
                            "Persisted developer reference is invalid: "
                                    + exception.getMessage()
                    ),
                    false
            );
            return 0;
        }

        var registry = WorldRpgContentRuntime.publisher()
                .active()
                .registries()
                .get(pointer.registryId());

        boolean resolved = registry != null
                && registry.asMap().containsKey(pointer.definitionId());

        player.sendMessage(
                Text.literal(
                        "Persisted developer reference | registry="
                                + pointer.registryId()
                                + " definition="
                                + pointer.definitionId()
                                + " resolved="
                                + resolved
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int clearRef(ServerPlayerEntity player) {
        WorldRpgPersistentState state =
                WorldRpgPersistentState.get(player.getServer());
        NbtCompound data =
                state.readPlayerData(player.getUuid());

        boolean existed = data.contains(PROBE_REF);
        data.remove(PROBE_REF);
        state.writePlayerData(player.getUuid(), data);

        player.sendMessage(
                Text.literal(
                        existed
                                ? "Cleared persisted developer reference."
                                : "No persisted developer reference to clear."
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }
}
