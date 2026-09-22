package dev.worldrpg.combat.condition;

import dev.worldrpg.api.id.RpgId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ConditionResult(List<ConditionFailure> failures) {
    public ConditionResult {
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }

    public static ConditionResult pass() {
        return new ConditionResult(List.of());
    }

    public static ConditionResult fail(RpgId code, String message) {
        return new ConditionResult(List.of(new ConditionFailure(code, message)));
    }

    public boolean passed() {
        return failures.isEmpty();
    }

    public ConditionResult plus(ConditionResult other) {
        Objects.requireNonNull(other, "other");

        if (passed()) {
            return other;
        }
        if (other.passed()) {
            return this;
        }

        List<ConditionFailure> combined = new ArrayList<>(failures);
        combined.addAll(other.failures);
        return new ConditionResult(combined);
    }
}
