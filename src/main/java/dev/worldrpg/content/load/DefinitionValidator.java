package dev.worldrpg.content.load;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

@FunctionalInterface
public interface DefinitionValidator<T extends RpgDefinition> {
    void validate(
            T definition,
            RegistrySnapshot candidate,
            SourceRef source,
            ValidationReport report
    );

    static <T extends RpgDefinition> DefinitionValidator<T> none() {
        return (definition, candidate, source, report) -> {
        };
    }
}
