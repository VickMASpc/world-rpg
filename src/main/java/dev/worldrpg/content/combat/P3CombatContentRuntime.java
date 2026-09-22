package dev.worldrpg.content.combat;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.content.load.ContentLoadResult;

import java.util.concurrent.atomic.AtomicReference;

public final class P3CombatContentRuntime {
    private static final AtomicReference<P3CombatContentSnapshot> ACTIVE =
            new AtomicReference<>(P3CombatContentSnapshot.empty());

    private static boolean registered;

    private P3CombatContentRuntime() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        WorldRpgContentRuntime.addResultListener(
                P3CombatContentRuntime::accept
        );
    }

    public static P3CombatContentSnapshot active() {
        return ACTIVE.get();
    }

    private static void accept(ContentLoadResult result) {
        if (!result.published()) {
            return;
        }

        P3CombatContentSnapshot compiled =
                P3CombatContentCompiler.compile(result.candidate());

        ACTIVE.set(compiled);

        WorldRpg.LOGGER.info(
                "Compiled World RPG combat content: {} auras, {} abilities.",
                compiled.auras().size(),
                compiled.abilities().size()
        );
    }
}
