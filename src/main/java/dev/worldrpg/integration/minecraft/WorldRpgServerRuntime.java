package dev.worldrpg.integration.minecraft;

import dev.worldrpg.integration.minecraft.p3.P3DeveloperCombatRuntime;
import dev.worldrpg.integration.minecraft.p3.P3DeveloperRoom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class WorldRpgServerRuntime {
    private static final MinecraftCombatActorBindings ACTORS =
            new MinecraftCombatActorBindings();
    private static final P3DeveloperCombatRuntime P3_COMBAT =
            new P3DeveloperCombatRuntime(ACTORS);
    private static final P3DeveloperRoom P3_ROOM =
            new P3DeveloperRoom(P3_COMBAT);

    private static boolean registered;

    private WorldRpgServerRuntime() {
    }

    public static MinecraftCombatActorBindings actors() {
        return ACTORS;
    }

    public static P3DeveloperCombatRuntime p3Combat() {
        return P3_COMBAT;
    }

    public static P3DeveloperRoom p3Room() {
        return P3_ROOM;
    }

    public static void registerLifecycle() {
        if (registered) {
            return;
        }
        registered = true;

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            P3_COMBAT.start(server);
            P3_ROOM.start(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(
                P3_COMBAT::tick
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            P3_ROOM.stop();
            P3_COMBAT.stop();
            ACTORS.clear();
        });
    }
}
