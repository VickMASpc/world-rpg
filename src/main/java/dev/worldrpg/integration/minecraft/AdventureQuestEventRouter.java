package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.QuestContentDefinition;
import dev.worldrpg.content.adventure.QuestObjectiveSpec;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.quest.PlayerQuestLog;
import dev.worldrpg.quest.QuestObjectiveSequence;
import dev.worldrpg.quest.QuestProgress;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Converts physical adventure events into ordered quest-state transitions.
 *
 * <p>NPC interactions are intentionally single-transition: one physical
 * interaction can complete one speak objective, turn in one quest, or accept
 * one quest, never multiple states at once.</p>
 */
public final class AdventureQuestEventRouter {
    public EventResult onNpcInteracted(
            ServerPlayerEntity player,
            RpgId npcId
    ) {
        List<QuestContentDefinition> definitions = definitions();
        PlayerQuestLog log = MinecraftQuestRuntime.load(player);

        // Current authored objective always wins. This prevents later
        // speak-to-NPC objectives from being completed out of order.
        for (QuestContentDefinition definition : definitions) {
            Optional<QuestProgress> progress = log.find(definition.id());
            if (progress.isEmpty()) {
                continue;
            }

            Optional<QuestObjectiveSpec> current =
                    QuestObjectiveSequence.firstIncomplete(
                            definition,
                            progress.orElseThrow()
                    );

            if (current.isPresent()
                    && current.orElseThrow()
                    instanceof QuestObjectiveSpec.SpeakToNpc speak
                    && speak.npc().id().equals(npcId)) {
                var advance = MinecraftQuestRuntime.completeObjective(
                        player,
                        definition.id(),
                        speak.key()
                );
                if (advance == MinecraftQuestRuntime.AdvanceResult.ADVANCED
                        || advance
                        == MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN) {
                    return new EventResult(
                            advance
                                    == MinecraftQuestRuntime.AdvanceResult
                                    .READY_TO_TURN_IN
                                    ? EventKind.OBJECTIVE_COMPLETED_READY
                                    : EventKind.OBJECTIVE_COMPLETED,
                            definition,
                            speak.key()
                    );
                }
            }
        }

        // A ready quest turns in before this NPC offers another quest.
        for (QuestContentDefinition definition : definitions) {
            if (!definition.turnIn().id().equals(npcId)) {
                continue;
            }

            var view = MinecraftQuestRuntime.view(
                    player,
                    definition.id()
            );
            if (view.state()
                    != MinecraftQuestRuntime.QuestState.READY_TO_TURN_IN) {
                continue;
            }

            var result = MinecraftQuestRuntime.turnIn(
                    player,
                    definition.id()
            );
            if (result == MinecraftQuestRuntime.TurnInResult.TURNED_IN) {
                return new EventResult(
                        EventKind.QUEST_TURNED_IN,
                        definition,
                        null
                );
            }
        }

        // Finally offer one deterministic not-yet-started quest. A locked
        // future quest must not suppress another quest that is currently
        // eligible from the same NPC.
        EventResult lockedCandidate = null;
        for (QuestContentDefinition definition : definitions) {
            if (!definition.starter().id().equals(npcId)) {
                continue;
            }

            var view = MinecraftQuestRuntime.view(
                    player,
                    definition.id()
            );
            if (view.state()
                    != MinecraftQuestRuntime.QuestState.NOT_ACTIVE) {
                continue;
            }

            var result = MinecraftQuestRuntime.accept(
                    player,
                    definition.id()
            );
            if (result == MinecraftQuestRuntime.AcceptResult.ACCEPTED) {
                return new EventResult(
                        EventKind.QUEST_ACCEPTED,
                        definition,
                        null
                );
            }
            if (result
                    == MinecraftQuestRuntime.AcceptResult
                    .PREREQUISITES_INCOMPLETE
                    && lockedCandidate == null) {
                lockedCandidate = new EventResult(
                        EventKind.QUEST_LOCKED,
                        definition,
                        null
                );
            }
        }

        return lockedCandidate == null
                ? EventResult.none()
                : lockedCandidate;
    }

    public List<EventResult> onMobDefeated(
            ServerPlayerEntity player,
            RpgId mobId
    ) {
        List<EventResult> results = new ArrayList<>();
        PlayerQuestLog log = MinecraftQuestRuntime.load(player);

        for (QuestContentDefinition definition : definitions()) {
            Optional<QuestProgress> progress =
                    log.find(definition.id());
            if (progress.isEmpty()) {
                continue;
            }

            Optional<QuestObjectiveSpec> current =
                    QuestObjectiveSequence.firstIncomplete(
                            definition,
                            progress.orElseThrow()
                    );

            if (current.isEmpty()
                    || !(current.orElseThrow()
                    instanceof QuestObjectiveSpec.DefeatMob defeat)
                    || !defeat.mob().id().equals(mobId)) {
                continue;
            }

            var advance = MinecraftQuestRuntime.completeObjective(
                    player,
                    definition.id(),
                    defeat.key()
            );
            if (advance == MinecraftQuestRuntime.AdvanceResult.ADVANCED
                    || advance
                    == MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN) {
                results.add(new EventResult(
                        advance
                                == MinecraftQuestRuntime.AdvanceResult
                                .READY_TO_TURN_IN
                                ? EventKind.OBJECTIVE_COMPLETED_READY
                                : EventKind.OBJECTIVE_COMPLETED,
                        definition,
                        defeat.key()
                ));
            }
        }

        return List.copyOf(results);
    }

    public List<EventResult> onLocationEntered(
            ServerPlayerEntity player,
            RpgId locationId
    ) {
        List<EventResult> results = new ArrayList<>();
        PlayerQuestLog log = MinecraftQuestRuntime.load(player);

        for (QuestContentDefinition definition : definitions()) {
            Optional<QuestProgress> progress = log.find(definition.id());
            if (progress.isEmpty()) {
                continue;
            }

            Optional<QuestObjectiveSpec> current =
                    QuestObjectiveSequence.firstIncomplete(
                            definition,
                            progress.orElseThrow()
                    );

            if (current.isEmpty()
                    || !(current.orElseThrow()
                    instanceof QuestObjectiveSpec.VisitLocation visit)
                    || !visit.location().id().equals(locationId)) {
                continue;
            }

            var advance = MinecraftQuestRuntime.completeObjective(
                    player,
                    definition.id(),
                    visit.key()
            );
            if (advance == MinecraftQuestRuntime.AdvanceResult.ADVANCED
                    || advance
                    == MinecraftQuestRuntime.AdvanceResult.READY_TO_TURN_IN) {
                results.add(new EventResult(
                        advance
                                == MinecraftQuestRuntime.AdvanceResult
                                .READY_TO_TURN_IN
                                ? EventKind.OBJECTIVE_COMPLETED_READY
                                : EventKind.OBJECTIVE_COMPLETED,
                        definition,
                        visit.key()
                ));
            }
        }

        return List.copyOf(results);
    }

    public Optional<QuestObjectiveSpec> currentObjective(
            ServerPlayerEntity player,
            QuestContentDefinition definition
    ) {
        return MinecraftQuestRuntime.load(player)
                .find(definition.id())
                .flatMap(progress ->
                        QuestObjectiveSequence.firstIncomplete(
                                definition,
                                progress
                        )
                );
    }

    private static List<QuestContentDefinition> definitions() {
        return WorldRpgContentRuntime.publisher()
                .active()
                .require(AdventureContentDomains.QUESTS)
                .values()
                .stream()
                .sorted(Comparator.comparing(
                        definition -> definition.id().toString()
                ))
                .toList();
    }

    public enum EventKind {
        NONE,
        QUEST_ACCEPTED,
        QUEST_LOCKED,
        OBJECTIVE_COMPLETED,
        OBJECTIVE_COMPLETED_READY,
        QUEST_TURNED_IN
    }

    public record EventResult(
            EventKind kind,
            QuestContentDefinition quest,
            String objectiveKey
    ) {
        public static EventResult none() {
            return new EventResult(
                    EventKind.NONE,
                    null,
                    null
            );
        }

        public boolean changedState() {
            return kind != EventKind.NONE;
        }
    }
}
