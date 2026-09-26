package dev.worldrpg.client.combat;

import dev.worldrpg.network.combat.CombatStateS2CPayload;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class WorldRpgCombatHud {
    private static boolean registered;

    private WorldRpgCombatHud() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        HudRenderCallback.EVENT.register(
                (context, tickCounter) -> render(context)
        );
    }

    private static void render(DrawContext context) {
        MinecraftClient client =
                MinecraftClient.getInstance();
        if (client.player == null
                || client.world == null
                || client.options.hudHidden) {
            return;
        }

        long tick = client.world.getTime();
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        CombatStateS2CPayload.ActorState self =
                CombatHudState.self();
        if (self != null) {
            int x = 18;
            int y = height - 55;
            drawActorPanel(
                    context,
                    client,
                    self,
                    x,
                    y,
                    150,
                    true
            );
        }

        CombatStateS2CPayload.ActorState target =
                CombatHudState.target();
        if (target != null) {
            int panelWidth = 180;
            drawActorPanel(
                    context,
                    client,
                    target,
                    (width - panelWidth) / 2,
                    18,
                    panelWidth,
                    false
            );
        }

        int actionWidth = 108;
        int actionX = (width - actionWidth) / 2;
        int actionY = height - 42;
        context.fill(
                actionX,
                actionY,
                actionX + actionWidth,
                actionY + 24,
                0xB0101010
        );
        context.fill(
                actionX,
                actionY,
                actionX + 3,
                actionY + 24,
                0xFFE4B05A
        );
        context.drawTextWithShadow(
                client.textRenderer,
                "[G] Field Strike",
                actionX + 9,
                actionY + 8,
                0xFFF2E6C9
        );

        String feedback =
                CombatHudState.feedback(tick);
        if (!feedback.isBlank()) {
            int color = CombatHudState.feedbackAccepted()
                    ? 0xFFB7E08A
                    : 0xFFFF8A7A;
            int x = (width
                    - client.textRenderer.getWidth(feedback))
                    / 2;
            context.drawTextWithShadow(
                    client.textRenderer,
                    feedback,
                    x,
                    actionY - 14,
                    color
            );
        }

        String loot =
                CombatHudState.lootNotice(tick);
        if (!loot.isBlank()) {
            int x = width
                    - client.textRenderer.getWidth(loot)
                    - 18;
            context.fill(
                    x - 7,
                    45,
                    width - 11,
                    64,
                    0xB0181510
            );
            context.drawTextWithShadow(
                    client.textRenderer,
                    loot,
                    x,
                    51,
                    0xFFFFD78A
            );
        }
    }

    private static void drawActorPanel(
            DrawContext context,
            MinecraftClient client,
            CombatStateS2CPayload.ActorState actor,
            int x,
            int y,
            int width,
            boolean showFocus
    ) {
        int height = showFocus ? 37 : 28;
        context.fill(
                x,
                y,
                x + width,
                y + height,
                0xB0101010
        );

        context.drawTextWithShadow(
                client.textRenderer,
                actor.displayName()
                        + "  Lv. " + actor.level(),
                x + 6,
                y + 5,
                0xFFF0E8D8
        );

        drawBar(
                context,
                x + 6,
                y + 17,
                width - 12,
                7,
                actor.health(),
                actor.maximumHealth(),
                0xFF9F2F37
        );

        if (showFocus) {
            drawBar(
                    context,
                    x + 6,
                    y + 27,
                    width - 12,
                    5,
                    actor.focus(),
                    actor.maximumFocus(),
                    0xFFD4A94E
            );
        }

        if (actor.activeAbilityId() != null) {
            String cast = shortId(
                    actor.activeAbilityId().toString()
            );
            context.drawTextWithShadow(
                    client.textRenderer,
                    cast,
                    x + width
                            - client.textRenderer.getWidth(cast)
                            - 6,
                    y + 5,
                    0xFFAAD7FF
            );
        }
    }

    private static void drawBar(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            double value,
            double maximum,
            int fillColor
    ) {
        context.fill(
                x,
                y,
                x + width,
                y + height,
                0xFF302B29
        );

        double ratio = maximum <= 0.0
                ? 0.0
                : Math.max(
                        0.0,
                        Math.min(1.0, value / maximum)
                );
        int filled =
                (int) Math.round(width * ratio);
        if (filled > 0) {
            context.fill(
                    x,
                    y,
                    x + filled,
                    y + height,
                    fillColor
            );
        }
    }

    private static String shortId(String id) {
        int slash = id.lastIndexOf('/');
        return slash >= 0
                ? id.substring(slash + 1)
                : id;
    }
}
