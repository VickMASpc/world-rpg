package dev.worldrpg.integration.minecraft;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class WorldRpgServerRuntime {
    private static final MinecraftCombatActorBindings ACTORS =
            new MinecraftCombatActorBindings();

    private static boolean registered;

    private WorldRpgServerRuntime() {
    }

    public static MinecraftCombatActorBindings actors() {
        return ACTORS;
    }

    public static void registerLifecycle() {
        if (registered) {
            return;
        }
        registered = true;

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTORS.clear());
    }
}
