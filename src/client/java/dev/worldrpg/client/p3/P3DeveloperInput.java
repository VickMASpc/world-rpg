package dev.worldrpg.client.p3;

import dev.worldrpg.client.network.P3ClientAbilitySender;
import dev.worldrpg.integration.minecraft.p3.P3FixtureDefinitions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Temporary P3 developer input surface.
 *
 * <p>This is deliberately not the final action-bar/keybind architecture.</p>
 */
public final class P3DeveloperInput {
    private static final String CATEGORY =
            "key.categories.world_rpg.p3";

    private static final KeyBinding FOCUS =
            KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.world_rpg.p3.focus",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F6,
                    CATEGORY
            ));

    private static final KeyBinding BOLT =
            KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.world_rpg.p3.bolt",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F7,
                    CATEGORY
            ));

    private static final KeyBinding CHANNEL =
            KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.world_rpg.p3.channel",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F8,
                    CATEGORY
            ));

    private static boolean registered;

    private P3DeveloperInput() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(
                P3DeveloperInput::onClientTick
        );
    }

    private static void onClientTick(MinecraftClient client) {
        while (FOCUS.wasPressed()) {
            sendFocus(client);
        }

        while (BOLT.wasPressed()) {
            sendTargeted(
                    client,
                    P3FixtureDefinitions.BOLT,
                    "Bolt"
            );
        }

        while (CHANNEL.wasPressed()) {
            sendTargeted(
                    client,
                    P3FixtureDefinitions.CHANNEL,
                    "Channel"
            );
        }
    }

    private static void sendFocus(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        P3ClientAbilitySender.send(
                P3FixtureDefinitions.FOCUS,
                client.player.getUuid()
        );
    }

    private static void sendTargeted(
            MinecraftClient client,
            dev.worldrpg.api.id.RpgId abilityId,
            String label
    ) {
        if (client.player == null) {
            return;
        }

        if (!(client.crosshairTarget instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target)) {
            client.player.sendMessage(
                    Text.literal(
                            "P3 " + label
                                    + ": look directly at a living target."
                    ),
                    false
            );
            return;
        }

        P3ClientAbilitySender.send(
                abilityId,
                target.getUuid()
        );
    }
}
