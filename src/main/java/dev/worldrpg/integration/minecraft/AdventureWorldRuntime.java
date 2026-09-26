package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.adventure.NpcContentDefinition;
import dev.worldrpg.content.adventure.WorldLocationContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.quest.fabric.MinecraftQuestRuntime;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Generic physical adventure bridge: entity interactions and location entry
 * become authored quest events.
 */
public final class AdventureWorldRuntime {
    private static final int LOCATION_SCAN_INTERVAL_TICKS = 5;
    private static final int INTERACTION_DEBOUNCE_TICKS = 4;

    private final AdventureWorldBindings bindings =
            new AdventureWorldBindings();
    private final AdventureQuestEventRouter questEvents =
            new AdventureQuestEventRouter();

    private final Map<UUID, Set<RpgId>> occupiedLocations =
            new HashMap<>();
    private final Map<InteractionKey, Long> lastInteractionTick =
            new HashMap<>();

    private MinecraftServer server;
    private long ticks;
    private boolean interactionRegistered;

    public AdventureWorldBindings bindings() {
        return bindings;
    }

    public AdventureQuestEventRouter questEvents() {
        return questEvents;
    }

    public void registerInteraction() {
        if (interactionRegistered) {
            return;
        }
        interactionRegistered = true;

        UseEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {
                    if (!(player instanceof ServerPlayerEntity serverPlayer)
                            || hand != Hand.MAIN_HAND) {
                        return ActionResult.PASS;
                    }

                    var npcId = bindings.npcId(entity.getUuid());
                    if (npcId.isEmpty()) {
                        return ActionResult.PASS;
                    }

                    InteractionKey key = new InteractionKey(
                            serverPlayer.getUuid(),
                            entity.getUuid()
                    );
                    Long previous = lastInteractionTick.get(key);
                    if (previous != null
                            && ticks - previous
                            < INTERACTION_DEBOUNCE_TICKS) {
                        return ActionResult.SUCCESS;
                    }
                    lastInteractionTick.put(key, ticks);

                    var result = questEvents.onNpcInteracted(
                            serverPlayer,
                            npcId.orElseThrow()
                    );
                    presentNpcResult(
                            serverPlayer,
                            npcId.orElseThrow(),
                            result
                    );
                    return ActionResult.SUCCESS;
                }
        );
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        ticks = 0;
        occupiedLocations.clear();
        lastInteractionTick.clear();
        bindings.start(server);
    }

    public void stop() {
        bindings.stop();
        occupiedLocations.clear();
        lastInteractionTick.clear();
        server = null;
        ticks = 0;
    }

    public void tick(MinecraftServer server) {
        if (this.server != server) {
            return;
        }

        ticks++;
        if (ticks % LOCATION_SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        Set<UUID> online = new HashSet<>();
        for (ServerPlayerEntity player
                : server.getPlayerManager().getPlayerList()) {
            online.add(player.getUuid());

            Set<RpgId> current =
                    bindings.locationsContaining(player);
            Set<RpgId> previous = occupiedLocations.getOrDefault(
                    player.getUuid(),
                    Set.of()
            );

            for (RpgId locationId : current) {
                if (previous.contains(locationId)) {
                    continue;
                }

                var results = questEvents.onLocationEntered(
                        player,
                        locationId
                );
                for (var result : results) {
                    presentLocationResult(
                            player,
                            locationId,
                            result
                    );
                }
            }

            occupiedLocations.put(
                    player.getUuid(),
                    current
            );
        }

        occupiedLocations.keySet().retainAll(online);
        lastInteractionTick.keySet().removeIf(
                key -> !online.contains(key.playerUuid())
        );
    }

    public String statusSummary() {
        return "adventure world runtime | npcBindings="
                + bindings.npcBindingCount()
                + " locationBindings="
                + bindings.locationBindingCount();
    }

    private static void presentNpcResult(
            ServerPlayerEntity player,
            RpgId npcId,
            AdventureQuestEventRouter.EventResult result
    ) {
        String npcName = WorldRpgContentRuntime.publisher()
                .active()
                .require(AdventureContentDomains.NPCS)
                .find(npcId)
                .map(NpcContentDefinition::displayName)
                .orElse(npcId.toString());

        switch (result.kind()) {
            case QUEST_ACCEPTED -> {
                player.sendMessage(
                        Text.literal(
                                npcName + ": "
                                        + result.quest().journalSummary()
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Quest accepted: "
                                        + result.quest().title()
                        ),
                        false
                );
            }
            case QUEST_LOCKED -> {
                String prerequisites = result.quest()
                        .prerequisites()
                        .stream()
                        .map(prerequisite ->
                                MinecraftQuestRuntime.definition(
                                        prerequisite.id()
                                )
                                        .map(value -> value.title())
                                        .orElse(
                                                prerequisite.id()
                                                        .toString()
                                        )
                        )
                        .reduce((left, right) ->
                                left + ", " + right
                        )
                        .orElse("another quest");

                player.sendMessage(
                        Text.literal(
                                npcName
                                        + ": Finish what came before first."
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Quest locked: "
                                        + result.quest().title()
                                        + " | requires: "
                                        + prerequisites
                        ),
                        false
                );
            }
            case OBJECTIVE_COMPLETED -> {
                player.sendMessage(
                        Text.literal(
                                npcName
                                        + ": Good. Keep moving."
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Objective complete ["
                                        + result.quest().title()
                                        + "]: "
                                        + humanize(result.objectiveKey())
                        ),
                        false
                );
            }
            case OBJECTIVE_COMPLETED_READY -> {
                player.sendMessage(
                        Text.literal(
                                npcName
                                        + ": That's enough. We can settle this now."
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Objective complete ["
                                        + result.quest().title()
                                        + "]: "
                                        + humanize(result.objectiveKey())
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Quest ready to turn in. Interact again."
                        ),
                        false
                );
            }
            case QUEST_TURNED_IN -> {
                player.sendMessage(
                        Text.literal(
                                npcName
                                        + ": Done. Take your payment."
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Quest complete: "
                                        + result.quest().title()
                        ),
                        false
                );
                player.sendMessage(
                        Text.literal(
                                "Reward: "
                                        + rewardSummary(result.quest())
                        ),
                        false
                );
            }
            case NONE -> player.sendMessage(
                    Text.literal(
                            npcName
                                    + ": Nothing new for you right now."
                    ),
                    false
            );
        }
    }

    private static void presentLocationResult(
            ServerPlayerEntity player,
            RpgId locationId,
            AdventureQuestEventRouter.EventResult result
    ) {
        String locationName = WorldRpgContentRuntime.publisher()
                .active()
                .require(AdventureContentDomains.WORLD_LOCATIONS)
                .find(locationId)
                .map(WorldLocationContentDefinition::displayName)
                .orElse(locationId.toString());

        player.sendMessage(
                Text.literal(
                        "Entered RPG location: " + locationName
                ),
                false
        );
        player.sendMessage(
                Text.literal(
                        "Objective complete ["
                                + result.quest().title()
                                + "]: "
                                + humanize(result.objectiveKey())
                ),
                false
        );

        if (result.kind()
                == AdventureQuestEventRouter.EventKind
                .OBJECTIVE_COMPLETED_READY) {
            player.sendMessage(
                    Text.literal(
                            "Quest ready to turn in."
                    ),
                    false
            );
        }
    }

    private static String rewardSummary(
            dev.worldrpg.content.adventure.QuestContentDefinition definition
    ) {
        String items = definition.itemRewards()
                .stream()
                .map(reward -> {
                    String name = WorldRpgContentRuntime.publisher()
                            .active()
                            .require(AdventureContentDomains.ITEMS)
                            .find(reward.item().id())
                            .map(ItemContentDefinition::displayName)
                            .orElse(reward.item().id().toString());
                    return name + " x" + reward.quantity();
                })
                .reduce((left, right) -> left + ", " + right)
                .orElse("no item reward");

        return items + ", "
                + definition.copperReward()
                + " copper";
    }

    private static String humanize(String key) {
        if (key == null || key.isBlank()) {
            return "objective";
        }
        return key.replace('_', ' ')
                .replace('-', ' ');
    }

    private record InteractionKey(
            UUID playerUuid,
            UUID entityUuid
    ) {
    }
}
