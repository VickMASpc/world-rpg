package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.stat.StatKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfiledCombatStatResolverTest {
    private static final StatKey PRIMARY =
            StatKey.of("world_rpg:stat/test/primary");
    private static final StatKey POWER =
            StatKey.of("world_rpg:stat/test/power");
    private static final StatKey RATING =
            StatKey.of("world_rpg:stat/test/rating");
    private static final StatKey CHANCE =
            StatKey.of("world_rpg:stat/test/chance");

    @Test
    void derivedValueAddsDirectLinearAndRatingContributions() {
        CombatActor actor =
                new CombatActor(new CombatActorId(1));

        actor.stats().setBase(POWER, 5.0);
        actor.stats().setBase(PRIMARY, 10.0);
        actor.stats().setBase(CHANCE, 0.02);
        actor.stats().setBase(RATING, 20.0);

        LevelRatingCurve ratingCurve =
                new LevelRatingCurve(
                        100,
                        1.0,
                        8.0,
                        1.0
                );

        CombatDerivedStatProfile profile =
                CombatDerivedStatProfile.builder()
                        .addDerivedRule(
                                new DerivedStatRule(
                                        POWER,
                                        List.of(
                                                new LinearStatContribution(
                                                        PRIMARY,
                                                        0.5
                                                )
                                        )
                                )
                        )
                        .addRatingRule(
                                new RatingConversionRule(
                                        RATING,
                                        CHANCE,
                                        ratingCurve
                                )
                        )
                        .build();

        ProfiledCombatStatResolver resolver =
                new ProfiledCombatStatResolver(
                        profile,
                        CombatLevelSource.constant(100)
                );

        assertEquals(
                10.0,
                resolver.value(actor, POWER),
                0.0
        );

        // At level 100, 8 rating = 1%. 20 rating therefore contributes 2.5%.
        assertEquals(
                0.045,
                resolver.value(actor, CHANCE),
                1.0e-12
        );
    }

    @Test
    void sameRatingContributesLessAtHigherLevel() {
        CombatActor actor =
                new CombatActor(new CombatActorId(1));
        actor.stats().setBase(RATING, 16.0);

        LevelRatingCurve curve =
                new LevelRatingCurve(
                        100,
                        1.0,
                        8.0,
                        0.75
                );

        CombatDerivedStatProfile profile =
                CombatDerivedStatProfile.builder()
                        .addRatingRule(
                                new RatingConversionRule(
                                        RATING,
                                        CHANCE,
                                        curve
                                )
                        )
                        .build();

        double levelOne =
                new ProfiledCombatStatResolver(
                        profile,
                        CombatLevelSource.constant(1)
                ).value(actor, CHANCE);

        double levelHundred =
                new ProfiledCombatStatResolver(
                        profile,
                        CombatLevelSource.constant(100)
                ).value(actor, CHANCE);

        assertEquals(0.16, levelOne, 1.0e-12);
        assertEquals(0.02, levelHundred, 1.0e-12);
        assertTrue(levelOne > levelHundred);
    }

    @Test
    void referenceDraftCoversCoreSecondaryRatings() {
        CombatActor actor =
                new CombatActor(new CombatActorId(1));
        actor.stats().setBase(
                CombatMathStats.CRIT_RATING,
                8.0
        );
        actor.stats().setBase(
                CombatMathStats.HIT_RATING,
                8.0
        );
        actor.stats().setBase(
                CombatMathStats.HASTE_RATING,
                8.0
        );

        ProfiledCombatStatResolver resolver =
                new ProfiledCombatStatResolver(
                        WorldRpgRatingDraft.referenceProfile(),
                        CombatLevelSource.constant(100)
                );

        assertEquals(
                0.01,
                resolver.value(actor, CombatMathStats.CRIT_CHANCE),
                1.0e-12
        );
        assertEquals(
                0.01,
                resolver.value(actor, CombatMathStats.HIT_CHANCE),
                1.0e-12
        );
        assertEquals(
                0.01,
                resolver.value(actor, CombatMathStats.HASTE_BONUS),
                1.0e-12
        );
    }
}
