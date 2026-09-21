package dev.worldrpg.api.reference;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.DefinitionRegistry;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Optional;

public final class ReferenceResolver {
    private ReferenceResolver() {
    }

    public static <T extends RpgDefinition> Optional<T> resolve(
            RequiredDefinitionRef<T> reference,
            RegistrySnapshot snapshot,
            ValidationReport report,
            SourceRef source
    ) {
        return resolvePresent(reference.registry(), reference.id(), snapshot, report, source);
    }

    public static <T extends RpgDefinition> Optional<T> resolve(
            OptionalDefinitionRef<T> reference,
            RegistrySnapshot snapshot,
            ValidationReport report,
            SourceRef source
    ) {
        if (reference.id().isEmpty()) {
            return Optional.empty();
        }

        return resolvePresent(reference.registry(), reference.id().get(), snapshot, report, source);
    }

    private static <T extends RpgDefinition> Optional<T> resolvePresent(
            RegistryKey<T> registryKey,
            RpgId id,
            RegistrySnapshot snapshot,
            ValidationReport report,
            SourceRef source
    ) {
        Optional<DefinitionRegistry<T>> registry = snapshot.find(registryKey);
        if (registry.isEmpty()) {
            report.error(
                    "reference.missing_registry",
                    "Reference targets unavailable registry " + registryKey.id(),
                    source,
                    id
            );
            return Optional.empty();
        }

        Optional<T> definition = registry.get().find(id);
        if (definition.isEmpty()) {
            report.error(
                    "reference.missing_definition",
                    "Missing referenced definition " + id + " in registry " + registryKey.id(),
                    source,
                    id
            );
        }

        return definition;
    }
}
