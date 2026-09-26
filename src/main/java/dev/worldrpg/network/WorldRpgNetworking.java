package dev.worldrpg.network;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionFailure;
import dev.worldrpg.integration.minecraft.MinecraftEntityResolver;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import dev.worldrpg.integration.minecraft.combat.ProductionAbilityActivationResponse;
import dev.worldrpg.integration.minecraft.p3.P3AbilityActivationResponse;
import dev.worldrpg.network.combat.CombatAbilityActivateC2SPayload;
import dev.worldrpg.network.combat.CombatStateRequestC2SPayload;
import dev.worldrpg.network.combat.CombatStateS2CPayload;
import dev.worldrpg.network.combat.LootNoticeS2CPayload;
import dev.worldrpg.network.p3.P3AbilityActivateC2SPayload;
import dev.worldrpg.network.p3.P3AbilityActivateS2CPayload;
import dev.worldrpg.network.p3.P3RequestSequenceTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public final class WorldRpgNetworking {
    private static final RpgId STALE_REQUEST =
            RpgId.parse("world_rpg:condition/stale_request_sequence");
    private static final RpgId INVALID_REQUEST =
            RpgId.parse("world_rpg:condition/invalid_activation_request");

    private static final P3RequestSequenceTracker P3_SEQUENCES =
            new P3RequestSequenceTracker();
    private static final P3RequestSequenceTracker COMBAT_SEQUENCES =
            new P3RequestSequenceTracker();

    private static boolean registered;

    private WorldRpgNetworking() {
    }

    public static void registerCommon() {
        if (registered) {
            return;
        }
        registered = true;

        PayloadTypeRegistry.playC2S().register(
                P3AbilityActivateC2SPayload.ID,
                P3AbilityActivateC2SPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                P3AbilityActivateS2CPayload.ID,
                P3AbilityActivateS2CPayload.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                CombatAbilityActivateC2SPayload.ID,
                CombatAbilityActivateC2SPayload.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                CombatStateRequestC2SPayload.ID,
                CombatStateRequestC2SPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                CombatStateS2CPayload.ID,
                CombatStateS2CPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                LootNoticeS2CPayload.ID,
                LootNoticeS2CPayload.CODEC
        );

        registerP3Receiver();
        registerProductionCombatReceivers();

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    UUID playerId =
                            handler.getPlayer().getUuid();
                    P3_SEQUENCES.remove(playerId);
                    COMBAT_SEQUENCES.remove(playerId);
                }
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> {
                    P3_SEQUENCES.clear();
                    COMBAT_SEQUENCES.clear();
                }
        );

        WorldRpg.LOGGER.info(
                "World RPG combat and P3 networking registered."
        );
    }

    public static void sendLootNotice(
            ServerPlayerEntity player,
            String message
    ) {
        ServerPlayNetworking.send(
                player,
                new LootNoticeS2CPayload(message)
        );
    }

    private static void registerProductionCombatReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                CombatAbilityActivateC2SPayload.ID,
                (payload, context) ->
                        context.server().execute(() -> {
                            boolean accepted = false;
                            String feedback;

                            if (!COMBAT_SEQUENCES.accept(
                                    context.player().getUuid(),
                                    payload.sequence()
                            )) {
                                feedback =
                                        "That combat input was already handled.";
                            } else {
                                try {
                                    ProductionAbilityActivationResponse response =
                                            WorldRpgServerRuntime
                                                    .productionCombat()
                                                    .activatePlayer(
                                                            context.player(),
                                                            payload.abilityId(),
                                                            payload.targetEntityUuid()
                                                    );
                                    accepted = response.accepted();
                                    feedback = accepted
                                            ? "Field Strike"
                                            : response.failures()
                                            .stream()
                                            .findFirst()
                                            .map(ConditionFailure::message)
                                            .orElse(
                                                    "Ability rejected"
                                            );
                                } catch (NoSuchElementException
                                         | IllegalArgumentException
                                         | IllegalStateException exception) {
                                    feedback =
                                            exception.getMessage() == null
                                                    ? "Invalid combat action"
                                                    : exception.getMessage();
                                }
                            }

                            sendCombatState(
                                    context.player(),
                                    payload.targetEntityUuid(),
                                    accepted,
                                    feedback
                            );
                        })
        );

        ServerPlayNetworking.registerGlobalReceiver(
                CombatStateRequestC2SPayload.ID,
                (payload, context) ->
                        context.server().execute(() ->
                                sendCombatState(
                                        context.player(),
                                        payload.targetEntityUuid(),
                                        true,
                                        ""
                                )
                        )
        );
    }

    private static void sendCombatState(
            ServerPlayerEntity player,
            UUID targetUuid,
            boolean accepted,
            String feedback
    ) {
        var combat =
                WorldRpgServerRuntime.productionCombat();
        var selfSnapshot = combat.snapshot(player);

        LivingEntity target = targetUuid == null
                ? null
                : MinecraftEntityResolver.findLiving(
                        player.getServer(),
                        targetUuid
                ).orElse(null);

        CombatStateS2CPayload.ActorState targetState = null;
        if (target != null) {
            targetState = combat.snapshotIfCombatant(target)
                    .map(snapshot ->
                            CombatStateS2CPayload.actor(
                                    snapshot,
                                    target.getName().getString()
                            )
                    )
                    .orElse(null);
        }

        ServerPlayNetworking.send(
                player,
                new CombatStateS2CPayload(
                        player.getServer().getTicks(),
                        CombatStateS2CPayload.actor(
                                selfSnapshot,
                                player.getName().getString()
                        ),
                        targetState,
                        accepted,
                        feedback
                )
        );
    }

    private static void registerP3Receiver() {
        ServerPlayNetworking.registerGlobalReceiver(
                P3AbilityActivateC2SPayload.ID,
                (payload, context) -> context.server().execute(() -> {
                    P3AbilityActivationResponse response;

                    if (!P3_SEQUENCES.accept(
                            context.player().getUuid(),
                            payload.sequence()
                    )) {
                        response = rejected(
                                payload.sequence(),
                                STALE_REQUEST,
                                "Activation request sequence is stale or duplicated"
                        );
                    } else {
                        try {
                            response = WorldRpgServerRuntime.p3Combat().handle(
                                    context.player(),
                                    payload.toRequest()
                            );
                        } catch (NoSuchElementException
                                 | IllegalArgumentException exception) {
                            response = rejected(
                                    payload.sequence(),
                                    INVALID_REQUEST,
                                    exception.getMessage() == null
                                            ? "Invalid activation request"
                                            : exception.getMessage()
                            );
                        }
                    }

                    ServerPlayNetworking.send(
                            context.player(),
                            P3AbilityActivateS2CPayload.from(response)
                    );
                })
        );
    }

    private static P3AbilityActivationResponse rejected(
            long sequence,
            RpgId code,
            String message
    ) {
        return new P3AbilityActivationResponse(
                sequence,
                false,
                List.of(new ConditionFailure(code, message)),
                false
        );
    }
}
