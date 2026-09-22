package dev.worldrpg.combat.ability;

/**
 * Movement behavior while a non-instant ability owns an active cast.
 *
 * <p>This is mechanism policy, not class balance. Individual abilities choose
 * whether movement breaks commitment.</p>
 */
public enum AbilityMovementPolicy {
    ALLOW,
    INTERRUPT
}
