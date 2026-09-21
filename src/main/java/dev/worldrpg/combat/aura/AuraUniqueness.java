package dev.worldrpg.combat.aura;

public enum AuraUniqueness {
    /**
     * At most one instance of this definition on a target.
     */
    PER_TARGET,

    /**
     * Each source actor may maintain its own instance on the target.
     */
    PER_SOURCE,

    /**
     * Every application creates an independent runtime instance.
     */
    INDEPENDENT
}
