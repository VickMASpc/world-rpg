package dev.worldrpg.api.validation;

import dev.worldrpg.api.id.RpgId;

import java.util.Objects;
import java.util.Optional;

public record ValidationMessage(
        ValidationSeverity severity,
        String code,
        String message,
        SourceRef source,
        Optional<RpgId> definitionId
) {
    public ValidationMessage {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(definitionId, "definitionId");

        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
