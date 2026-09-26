package dev.worldrpg.combat.math;

import dev.worldrpg.combat.stat.StatKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable conversion profile layered over direct P3 stats.
 *
 * <p>Profiles are intentionally compositional so class/loadout rules can
 * define primary-stat conversions without hard-coding one universal mapping
 * into the combat kernel.</p>
 */
public final class CombatDerivedStatProfile {
    private final Map<StatKey, List<LinearStatContribution>>
            linearByTarget;
    private final Map<StatKey, List<RatingConversionRule>>
            ratingsByTarget;

    private CombatDerivedStatProfile(
            Map<StatKey, List<LinearStatContribution>> linearByTarget,
            Map<StatKey, List<RatingConversionRule>> ratingsByTarget
    ) {
        this.linearByTarget = immutableNested(linearByTarget);
        this.ratingsByTarget = immutableNested(ratingsByTarget);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<LinearStatContribution> linearFor(
            StatKey targetStat
    ) {
        return linearByTarget.getOrDefault(
                Objects.requireNonNull(targetStat, "targetStat"),
                List.of()
        );
    }

    public List<RatingConversionRule> ratingsFor(
            StatKey targetStat
    ) {
        return ratingsByTarget.getOrDefault(
                Objects.requireNonNull(targetStat, "targetStat"),
                List.of()
        );
    }

    private static <T> Map<StatKey, List<T>> immutableNested(
            Map<StatKey, List<T>> input
    ) {
        Map<StatKey, List<T>> copy = new LinkedHashMap<>();
        input.forEach((key, value) ->
                copy.put(key, List.copyOf(value))
        );
        return java.util.Collections.unmodifiableMap(copy);
    }

    public static final class Builder {
        private final Map<StatKey, List<LinearStatContribution>>
                linearByTarget = new LinkedHashMap<>();
        private final Map<StatKey, List<RatingConversionRule>>
                ratingsByTarget = new LinkedHashMap<>();

        public Builder addDerivedRule(DerivedStatRule rule) {
            Objects.requireNonNull(rule, "rule");

            linearByTarget.computeIfAbsent(
                    rule.targetStat(),
                    ignored -> new ArrayList<>()
            ).addAll(rule.contributions());

            return this;
        }

        public Builder addRatingRule(RatingConversionRule rule) {
            Objects.requireNonNull(rule, "rule");

            ratingsByTarget.computeIfAbsent(
                    rule.targetStat(),
                    ignored -> new ArrayList<>()
            ).add(rule);

            return this;
        }

        public CombatDerivedStatProfile build() {
            return new CombatDerivedStatProfile(
                    linearByTarget,
                    ratingsByTarget
            );
        }
    }
}
