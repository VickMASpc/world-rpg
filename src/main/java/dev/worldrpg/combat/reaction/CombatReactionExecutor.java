package dev.worldrpg.combat.reaction;

import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatEventEnvelope;

import java.util.List;

@FunctionalInterface
public interface CombatReactionExecutor {
    /**
     * Executes one already-selected reaction and returns the child events
     * emitted by that gameplay mutation.
     */
    List<CombatEvent> execute(
            CombatReaction reaction,
            CombatEventEnvelope cause
    );
}
