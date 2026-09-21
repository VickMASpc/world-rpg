package dev.worldrpg.combat.condition;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record ConditionFailure(RpgId code, String message) {
    public ConditionFailure {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("condition failure message must not be blank");
        }
    }
}
