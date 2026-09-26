package dev.worldrpg.combat.absorb;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;

/**
 * Injected boundary between P4 damage mathematics and future shield/barrier
 * state ownership.
 *
 * <p>The gateway may mutate authoritative absorb state when resolve is called.
 * Simulator and runtime must use the same implementation.</p>
 */
@FunctionalInterface
public interface CombatAbsorbGateway {
    default ConditionResult validate(
            CombatMagnitudeRequest request
    ) {
        return ConditionResult.pass();
    }

    CombatAbsorbResult absorb(
            CombatMagnitudeRequest request,
            double incomingDamage
    );

    static CombatAbsorbGateway none() {
        return (request, incomingDamage) ->
                CombatAbsorbResult.none(incomingDamage);
    }
}
