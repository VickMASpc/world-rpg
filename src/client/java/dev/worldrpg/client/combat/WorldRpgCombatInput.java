package dev.worldrpg.client.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.client.network.CombatClientSender;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class WorldRpgCombatInput {
    public static final RpgId FIELD_STRIKE =
            RpgId.parse(
                    "world_rpg:ability/player/field_strike"
            );

    private static final String CATEGORY =
            "key.categories.world_rpg.combat";

    private static final KeyBinding FIELD_STRIKE_KEY =
            KeyBindingHelper.registerKeyBinding(
                    new KeyBinding(
                            "key.world_rpg.combat.field_strike",
                            InputUtil.Type.KEYSYM,
                            GLFW.GLFW_KEY_G,
                            CATEGORY
                    )
            );

    private static boolean registered;
    private static long ticks;

    private WorldRpgCombatInput() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(
                WorldRpgCombatInput::tick
        );
    }

    private static void tick(MinecraftClient client) {
        ticks++;

        while (FIELD_STRIKE_KEY.wasPressed()) {
            targeted(client);
        }

        if (client.player != null
                && client.world != null
                && ticks % 5L == 0L) {
            CombatClientSender.requestState(
                    crosshairTarget(client)
            );
        }
    }

    private static void targeted(
            MinecraftClient client
    ) {
        UUID target = crosshairTarget(client);
        if (target == null) {
            return;
        }
        CombatClientSender.activate(
                FIELD_STRIKE,
                target
        );
    }

    private static UUID crosshairTarget(
            MinecraftClient client
    ) {
        if (client.crosshairTarget
                instanceof EntityHitResult hit
                && hit.getEntity()
                instanceof LivingEntity living) {
            return living.getUuid();
        }
        return null;
    }
}
