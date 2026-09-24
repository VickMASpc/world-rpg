package dev.worldrpg.api.validation;

import dev.worldrpg.api.id.RpgId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable validation collector used while constructing a candidate snapshot.
 * Published registry snapshots never retain this mutable object.
 */
public final class ValidationReport {
    private final List<ValidationMessage> messages = new ArrayList<>();

    public void error(String code, String message, SourceRef source) {
        add(ValidationSeverity.ERROR, code, message, source, Optional.empty());
    }

    public void error(String code, String message, SourceRef source, RpgId definitionId) {
        add(ValidationSeverity.ERROR, code, message, source, Optional.of(definitionId));
    }

    public void warning(String code, String message, SourceRef source) {
        add(ValidationSeverity.WARNING, code, message, source, Optional.empty());
    }

    public void warning(String code, String message, SourceRef source, RpgId definitionId) {
        add(ValidationSeverity.WARNING, code, message, source, Optional.of(definitionId));
    }

    public void add(ValidationMessage message) {
        messages.add(Objects.requireNonNull(message, "message"));
    }

    public void merge(ValidationReport other) {
        Objects.requireNonNull(other, "other");
        messages.addAll(other.messages);
    }

    public List<ValidationMessage> messages() {
        return List.copyOf(messages);
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(message -> message.severity() == ValidationSeverity.ERROR);
    }

    public long errorCount() {
        return messages.stream().filter(message -> message.severity() == ValidationSeverity.ERROR).count();
    }

    public long warningCount() {
        return messages.stream().filter(message -> message.severity() == ValidationSeverity.WARNING).count();
    }

    private void add(
            ValidationSeverity severity,
            String code,
            String message,
            SourceRef source,
            Optional<RpgId> definitionId
    ) {
        add(new ValidationMessage(severity, code, message, source, definitionId));
    }
}
