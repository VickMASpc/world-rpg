package dev.worldrpg.combat.cast;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;

/**
 * Boundary used by generic effects that need to control another actor's active
 * cast without making CombatActor own CastController.
 */
public interface CombatCastControlGateway {
    ConditionResult validateInterrupt(
            CastInterruptionRequest request
    );

    List<CombatEvent> interrupt(
            CastInterruptionRequest request
    );
}
