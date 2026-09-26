package dev.worldrpg.combat.resolution;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;

@FunctionalInterface
public interface CombatResolutionGateway {
    default ConditionResult validate(
            CombatMagnitudeRequest request
    ) {
        return ConditionResult.pass();
    }

    List<CombatEvent> resolve(CombatMagnitudeRequest request);
}
