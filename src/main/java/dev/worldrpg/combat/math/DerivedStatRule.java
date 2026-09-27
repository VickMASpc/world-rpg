package dev.worldrpg.combat.math;

import dev.worldrpg.combat.stat.StatKey;

import java.util.List;
import java.util.Objects;

/**
 * Additive one-layer derived-stat rule.
 *
 * <p>Source stats are always read directly from the P3 StatSheet. Derived
 * rules do not recursively consume other derived rules, preventing hidden
 * cycles and order dependence.</p>
 */
public record DerivedStatRule(
        StatKey targetStat,
        List<LinearStatContribution> contributions
) {
    public DerivedStatRule {
        Objects.requireNonNull(targetStat, "targetStat");
        contributions = List.copyOf(
                Objects.requireNonNull(
                        contributions,
                        "contributions"
                )
        );
    }
}
