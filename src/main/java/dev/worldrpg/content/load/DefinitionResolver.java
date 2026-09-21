package dev.worldrpg.content.load;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

@FunctionalInterface
public interface DefinitionResolver<T extends RpgDefinition> {
    void resolve(
            T definition,
            RegistrySnapshot candidate,
            SourceRef source,
            ValidationReport report
    );

    static <T extends RpgDefinition> DefinitionResolver<T> none() {
        return (definition, candidate, source, report) -> {
        };
    }
}
