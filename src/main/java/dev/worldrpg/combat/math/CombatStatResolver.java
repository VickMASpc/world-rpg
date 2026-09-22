package dev.worldrpg.combat.math;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

/**
 * Read-only numerical stat view consumed by P4 formula code.
 *
 * <p>P3 owns raw runtime stat state. P4 may layer deterministic derived-stat
 * conversion over that state without mutating or duplicating the StatSheet.</p>
 */
@FunctionalInterface
public interface CombatStatResolver {
    double value(CombatActor actor, StatKey stat);

    static CombatStatResolver direct() {
        return (actor, stat) -> Objects.requireNonNull(actor, "actor")
                .stats()
                .value(Objects.requireNonNull(stat, "stat"));
    }
}
