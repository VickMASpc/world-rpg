package dev.worldrpg.combat.aura;

public enum AuraTickRefreshPolicy {
    /**
     * Reapplying/stacking the aura does not move its already-scheduled next tick.
     */
    KEEP_SCHEDULE,

    /**
     * Reapplying/stacking the aura schedules the next tick relative to refresh time.
     */
    RESET_SCHEDULE
}
