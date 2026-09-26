package dev.worldrpg.integration.minecraft;

import dev.worldrpg.integration.minecraft.combat.ProductionCombatRuntime;
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
    private static final AdventureWorldRuntime ADVENTURE_WORLD =
            new AdventureWorldRuntime();
    private static final AuthoredMobRuntime AUTHORED_MOBS =
            new AuthoredMobRuntime();
    private static final ProductionCombatRuntime PRODUCTION_COMBAT =
            new ProductionCombatRuntime();
    private static final FirstPlayableSliceRuntime PLAYABLE_SLICE =
            new FirstPlayableSliceRuntime(ADVENTURE_WORLD);

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

    public static AdventureWorldRuntime adventureWorld() {
        return ADVENTURE_WORLD;
    }

    public static AuthoredMobRuntime authoredMobs() {
        return AUTHORED_MOBS;
    }

    public static ProductionCombatRuntime productionCombat() {
        return PRODUCTION_COMBAT;
    }

    public static FirstPlayableSliceRuntime playableSlice() {
        return PLAYABLE_SLICE;
    }

    public static void registerLifecycle() {
        if (registered) {
            return;
        }
        registered = true;

        ADVENTURE_WORLD.registerInteraction();
        AUTHORED_MOBS.registerEvents();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            P3_COMBAT.start(server);
            P3_ROOM.start(server);
            ADVENTURE_WORLD.start(server);
            PRODUCTION_COMBAT.start(server);
            AUTHORED_MOBS.start(server);
            PLAYABLE_SLICE.start(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            P3_COMBAT.tick(server);
            PRODUCTION_COMBAT.tick(server);
            AUTHORED_MOBS.tick(server);
            ADVENTURE_WORLD.tick(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PLAYABLE_SLICE.stop();
            AUTHORED_MOBS.stop();
            PRODUCTION_COMBAT.stop();
            ADVENTURE_WORLD.stop();
            P3_ROOM.stop();
            P3_COMBAT.stop();
            ACTORS.clear();
        });
    }
}
