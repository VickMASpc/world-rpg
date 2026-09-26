package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.combat.condition.ConditionFailure;

import java.util.List;
import java.util.Objects;

public record ProductionAbilityActivationResponse(
        boolean accepted,
        boolean castStarted,
        List<ConditionFailure> failures
) {
    public ProductionAbilityActivationResponse {
        failures = List.copyOf(
                Objects.requireNonNull(failures, "failures")
        );
    }
}
