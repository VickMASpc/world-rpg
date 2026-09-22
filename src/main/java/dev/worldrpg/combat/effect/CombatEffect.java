package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;

/**
 * Generic gameplay effect.
 *
 * <p>Effects validate before an effect sequence mutates state. Apply should
 * therefore execute an already-valid operation and emit ordered events.</p>
 */
public interface CombatEffect {
    default ConditionResult validate(EffectContext context) {
        return ConditionResult.pass();
    }

    List<CombatEvent> apply(EffectContext context);
}
