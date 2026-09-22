package dev.worldrpg.combat.resolution;

/**
 * High-level mathematical outcome family.
 *
 * <p>P3 defines only the handoff category. P4 owns the formulas that turn a
 * request into a resolved outcome.</p>
 */
public enum CombatMagnitudeKind {
    DAMAGE,
    HEALING
}
