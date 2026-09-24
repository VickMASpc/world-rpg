package dev.worldrpg.api.data;

/**
 * Declares whether a definition domain may be replaced while a save is running.
 */
public enum ReloadSafety {
    /**
     * A validated replacement snapshot may be published live.
     */
    SAFE,

    /**
     * Live replacement requires domain-specific runtime guards.
     */
    GUARDED,

    /**
     * Changes require restart or save reload.
     */
    RESTART
}
