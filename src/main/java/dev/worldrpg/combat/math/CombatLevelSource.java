package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;

/**
 * Supplies progression level to level-aware combat mathematics.
 *
 * <p>CombatActor remains a generic P3 runtime object. Character/NPC ownership
 * layers decide where level is stored and provide it here.</p>
 */
@FunctionalInterface
public interface CombatLevelSource {
    int levelOf(CombatActor actor);

    static CombatLevelSource constant(int level) {
        if (level < 1) {
            throw new IllegalArgumentException(
                    "constant combat level must be >= 1"
            );
        }
        return actor -> level;
    }
}
