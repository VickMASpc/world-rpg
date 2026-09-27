package dev.worldrpg.combat.math;

/**
 * P4 reference secondary-rating calibration.
 *
 * <p>This is a simulator draft, not frozen gear-budget canon.</p>
 */
public final class WorldRpgRatingDraft {
    public static final LevelRatingCurve REFERENCE_RATING_PER_PERCENT =
            new LevelRatingCurve(
                    100,
                    1.0,
                    8.0,
                    0.75
            );

    private WorldRpgRatingDraft() {
    }

    public static CombatDerivedStatProfile referenceProfile() {
        CombatDerivedStatProfile.Builder builder =
                CombatDerivedStatProfile.builder();

        builder.addRatingRule(rule(
                CombatMathStats.CRIT_RATING,
                CombatMathStats.CRIT_CHANCE
        ));
        builder.addRatingRule(rule(
                CombatMathStats.HIT_RATING,
                CombatMathStats.HIT_CHANCE
        ));
        builder.addRatingRule(rule(
                CombatMathStats.HASTE_RATING,
                CombatMathStats.HASTE_BONUS
        ));
        builder.addRatingRule(rule(
                CombatMathStats.DODGE_RATING,
                CombatMathStats.DODGE_CHANCE
        ));
        builder.addRatingRule(rule(
                CombatMathStats.PARRY_RATING,
                CombatMathStats.PARRY_CHANCE
        ));
        builder.addRatingRule(rule(
                CombatMathStats.BLOCK_RATING,
                CombatMathStats.BLOCK_CHANCE
        ));

        return builder.build();
    }

    private static RatingConversionRule rule(
            dev.worldrpg.combat.stat.StatKey rating,
            dev.worldrpg.combat.stat.StatKey derived
    ) {
        return new RatingConversionRule(
                rating,
                derived,
                REFERENCE_RATING_PER_PERCENT
        );
    }
}
