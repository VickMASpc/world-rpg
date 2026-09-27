package dev.worldrpg.client.combat;

import dev.worldrpg.network.combat.CombatStateS2CPayload;

public final class CombatHudState {
    private static CombatStateS2CPayload.ActorState self;
    private static CombatStateS2CPayload.ActorState target;
    private static String feedback = "";
    private static boolean feedbackAccepted = true;
    private static long feedbackUntilClientTick;
    private static String lootNotice = "";
    private static long lootUntilClientTick;

    private CombatHudState() {
    }

    public static void update(
            CombatStateS2CPayload payload,
            long clientTick
    ) {
        self = payload.self();
        target = payload.target();

        if (!payload.feedback().isBlank()) {
            feedback = payload.feedback();
            feedbackAccepted = payload.accepted();
            feedbackUntilClientTick =
                    clientTick + 40L;
        }
    }

    public static void loot(
            String message,
            long clientTick
    ) {
        lootNotice = message;
        lootUntilClientTick = clientTick + 80L;
    }

    public static CombatStateS2CPayload.ActorState self() {
        return self;
    }

    public static CombatStateS2CPayload.ActorState target() {
        return target;
    }

    public static String feedback(long clientTick) {
        return clientTick <= feedbackUntilClientTick
                ? feedback
                : "";
    }

    public static boolean feedbackAccepted() {
        return feedbackAccepted;
    }

    public static String lootNotice(long clientTick) {
        return clientTick <= lootUntilClientTick
                ? lootNotice
                : "";
    }

    public static void clear() {
        self = null;
        target = null;
        feedback = "";
        feedbackUntilClientTick = 0L;
        lootNotice = "";
        lootUntilClientTick = 0L;
    }
}
