package dev.worldrpg.content.load;

import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.ValidationReport;

@FunctionalInterface
public interface CrossRegistryValidator {
    void validate(RegistrySnapshot candidate, ValidationReport report);
}
