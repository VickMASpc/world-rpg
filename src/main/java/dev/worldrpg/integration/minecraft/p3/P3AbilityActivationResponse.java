package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.combat.cast.AbilityActivationResult;
import dev.worldrpg.combat.condition.ConditionFailure;

import java.util.List;
import java.util.Objects;

public record P3AbilityActivationResponse(
        long sequence,
        boolean accepted,
        List<ConditionFailure> failures,
        boolean castStarted
) {
    public P3AbilityActivationResponse {
        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "response sequence must be >= 0"
            );
        }
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }

    public static P3AbilityActivationResponse from(
            long sequence,
            AbilityActivationResult result
    ) {
        Objects.requireNonNull(result, "result");

        return new P3AbilityActivationResponse(
                sequence,
                result.accepted(),
                result.validation().failures(),
                result.castId().isPresent()
        );
    }

    public String summary() {
        if (accepted) {
            return "accepted"
                    + (castStarted ? " (cast started)" : " (instant)");
        }

        return failures.isEmpty()
                ? "rejected"
                : "rejected: " + failures.get(0).message();
    }
}
