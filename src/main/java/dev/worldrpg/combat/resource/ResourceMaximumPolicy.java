package dev.worldrpg.combat.resource;

/**
 * Explicit behavior when maximum resource capacity changes.
 */
public enum ResourceMaximumPolicy {
    /**
     * Keep absolute current value, clamped to the new maximum.
     */
    KEEP_CURRENT,

    /**
     * Preserve current/max ratio when the previous maximum was non-zero.
     */
    PRESERVE_RATIO,

    /**
     * Preserve the absolute amount missing from the pool.
     */
    PRESERVE_MISSING_AMOUNT
}
