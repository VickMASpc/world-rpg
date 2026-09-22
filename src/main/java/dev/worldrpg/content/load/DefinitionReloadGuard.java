package dev.worldrpg.content.load;

import dev.worldrpg.api.registry.DefinitionRegistry;
import dev.worldrpg.api.validation.ValidationReport;

@FunctionalInterface
public interface DefinitionReloadGuard {
    void validate(
            DefinitionRegistry<?> active,
            DefinitionRegistry<?> candidate,
            ValidationReport report
    );
}
