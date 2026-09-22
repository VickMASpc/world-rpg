package dev.worldrpg.combat.math;

/**
 * Current contact result before magnitude scaling.
 *
 * <p>DODGE/PARRY/BLOCK remain later extensions once runtime eligibility facts
 * are carried explicitly. They are intentionally not inferred from school.</p>
 */
public enum CombatContactOutcome {
    HIT,
    MISS
}
