package dev.worldrpg.combat.cast;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AbilityActivationResult(
        ConditionResult validation,
        Optional<CastId> castId,
        List<CombatEvent> events
) {
    public AbilityActivationResult {
        Objects.requireNonNull(validation, "validation");
        Objects.requireNonNull(castId, "castId");
        events = List.copyOf(Objects.requireNonNull(events, "events"));
    }

    public boolean accepted() {
        return validation.passed();
    }
}
