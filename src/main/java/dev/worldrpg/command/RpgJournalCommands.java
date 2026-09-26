package dev.worldrpg.command;

import com.mojang.brigadier.Command;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.NpcContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.content.adventure.WorldLocationContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;

public final class RpgJournalCommands {
    private static boolean registered;

    private RpgJournalCommands() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("rpgjournal")
                                        .executes(context -> show(
                                                context.getSource()
                                                        .getPlayerOrThrow()
                                        ))
                        )
        );
    }

    private static int show(ServerPlayerEntity player) {
        var log = MinecraftQuestRuntime.load(player);

        if (log.activeCount() == 0) {
            player.sendMessage(
                    Text.literal(
                            "RPG Journal | no active quests | completed="
                                    + log.completedCount()
                    ),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        player.sendMessage(
                Text.literal(
                        "RPG Journal | active="
                                + log.activeCount()
                                + " completed="
                                + log.completedCount()
                ),
                false
        );

        log.activeQuestIds()
                .stream()
                .sorted(Comparator.comparing(RpgId::toString))
                .forEach(questId ->
                        printQuest(player, questId)
                );

        return Command.SINGLE_SUCCESS;
    }

    private static void printQuest(
            ServerPlayerEntity player,
            RpgId questId
    ) {
        var definition = MinecraftQuestRuntime.definition(
                questId
        );
        if (definition.isEmpty()) {
            player.sendMessage(
                    Text.literal(
                            questId
                                    + " | unresolved quest definition"
                    ),
                    false
            );
            return;
        }

        var value = definition.orElseThrow();
        var view = MinecraftQuestRuntime.view(
                player,
                questId
        );

        player.sendMessage(
                Text.literal(
                        value.title()
                                + " | " + view.state()
                                + " | "
                                + view.completedObjectives()
                                + "/"
                                + view.totalObjectives()
                ),
                false
        );
        player.sendMessage(
                Text.literal(value.journalSummary()),
                false
        );

        WorldRpgServerRuntime.adventureWorld()
                .questEvents()
                .currentObjective(player, value)
                .ifPresent(objective ->
                        player.sendMessage(
                                Text.literal(
                                        "Current objective: "
                                                + describe(objective)
                                ),
                                false
                        )
                );
    }

    private static String describe(
            QuestObjectiveSpec objective
    ) {
        if (objective
                instanceof QuestObjectiveSpec.VisitLocation visit) {
            String name = WorldRpgContentRuntime.publisher()
                    .active()
                    .require(
                            AdventureContentDomains.WORLD_LOCATIONS
                    )
                    .find(visit.location().id())
                    .map(
                            WorldLocationContentDefinition::displayName
                    )
                    .orElse(
                            visit.location().id().toString()
                    );
            return "Visit " + name;
        }

        QuestObjectiveSpec.SpeakToNpc speak =
                (QuestObjectiveSpec.SpeakToNpc) objective;
        String name = WorldRpgContentRuntime.publisher()
                .active()
                .require(AdventureContentDomains.NPCS)
                .find(speak.npc().id())
                .map(NpcContentDefinition::displayName)
                .orElse(speak.npc().id().toString());
        return "Speak to " + name;
    }
}
