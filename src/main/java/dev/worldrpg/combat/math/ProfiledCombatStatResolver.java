package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

/**
 * Adds deterministic derived-stat conversion over raw P3 StatSheet values.
 *
 * <p>The target stat's direct value remains the baseline. Profile rules add
 * primary/raw-stat contributions and level-aware rating contributions.</p>
 */
public final class ProfiledCombatStatResolver
        implements CombatStatResolver {
    private final CombatDerivedStatProfile profile;
    private final CombatLevelSource levels;

    public ProfiledCombatStatResolver(
            CombatDerivedStatProfile profile,
            CombatLevelSource levels
    ) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.levels = Objects.requireNonNull(levels, "levels");
    }

    @Override
    public double value(
            CombatActor actor,
            StatKey targetStat
    ) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(targetStat, "targetStat");

        double result = actor.stats().value(targetStat);

        for (LinearStatContribution contribution :
                profile.linearFor(targetStat)) {
            result += actor.stats()
                    .value(contribution.sourceStat())
                    * contribution.coefficient();
        }

        int level = levels.levelOf(actor);

        for (RatingConversionRule rule :
                profile.ratingsFor(targetStat)) {
            result += rule.curve().convert(
                    actor.stats().value(rule.ratingStat()),
                    level
            );
        }

        if (!Double.isFinite(result)) {
            throw new IllegalStateException(
                    "derived stat became non-finite: "
                            + targetStat
            );
        }

        return result;
    }
}
