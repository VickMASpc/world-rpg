package dev.worldrpg.combat.ability;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.target.TargetObservation;

@FunctionalInterface
public interface AbilityObservationProvider {
    TargetObservation observe(CombatActor source, CombatActor target);

    static AbilityObservationProvider unavailable() {
        return (source, target) -> TargetObservation.unavailable();
    }
}
