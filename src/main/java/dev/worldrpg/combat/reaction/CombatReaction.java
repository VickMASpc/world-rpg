package dev.worldrpg.combat.reaction;

/**
 * Marker for an immutable request to perform gameplay as a reaction to an event.
 *
 * <p>Listeners describe reactions. They do not execute gameplay mutations
 * directly. Execution belongs to a CombatReactionExecutor.</p>
 */
public interface CombatReaction {
}
