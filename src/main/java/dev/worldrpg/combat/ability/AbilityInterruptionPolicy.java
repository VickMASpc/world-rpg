package dev.worldrpg.combat.ability;

/**
 * Whether an active timed/channel cast accepts explicit INTERRUPT effects.
 *
 * <p>This is distinct from movement policy. A cast can be stationary but
 * uninterruptible, or movement-tolerant but interruptible.</p>
 */
public enum AbilityInterruptionPolicy {
    INTERRUPTIBLE,
    UNINTERRUPTIBLE
}
