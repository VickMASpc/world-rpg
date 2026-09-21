package dev.worldrpg.combat.event;

/**
 * Marker for deterministic gameplay events emitted by the RPG kernel.
 *
 * <p>Presentation/network layers consume events but do not determine outcomes.</p>
 */
public interface CombatEvent {
    long gameTick();
}
