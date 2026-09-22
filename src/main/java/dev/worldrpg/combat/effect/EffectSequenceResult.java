package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;

public record EffectSequenceResult(
        ConditionResult validation,
        List<CombatEvent> events
) {
    public EffectSequenceResult {
        Objects.requireNonNull(validation, "validation");
        events = List.copyOf(Objects.requireNonNull(events, "events"));
    }

    public boolean applied() {
        return validation.passed();
    }
}
