package dev.worldrpg.combat.math;

/**
 * Explicit stochastic input to combat mathematics.
 *
 * <p>Formula code must receive a roll source; it must not reach for global or
 * thread-local randomness. This keeps runtime and simulator behavior
 * reproducible from the same seed/roll stream.</p>
 */
@FunctionalInterface
public interface CombatRollSource {
    /**
     * Returns the next deterministic unit roll in [0, 1).
     */
    double nextUnit();
}
