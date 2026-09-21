package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Candidate builder for a single definition registry.
 */
public final class DefinitionRegistryBuilder<T extends RpgDefinition> {
    private final RegistryKey<T> key;
    private final Map<RpgId, T> definitions = new LinkedHashMap<>();
    private final Map<RpgId, SourceRef> sources = new LinkedHashMap<>();

    public DefinitionRegistryBuilder(RegistryKey<T> key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    public boolean add(T definition, SourceRef source, ValidationReport report) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(report, "report");

        RpgId id = Objects.requireNonNull(definition.id(), "definition.id()");
        SourceRef existingSource = sources.get(id);

        if (existingSource != null) {
            report.error(
                    "registry.duplicate_id",
                    "Duplicate definition " + id + " in registry " + key.id()
                            + "; first declared at " + existingSource.display(),
                    source,
                    id
            );
            return false;
        }

        definitions.put(id, definition);
        sources.put(id, source);
        return true;
    }

    public DefinitionRegistry<T> build() {
        return new DefinitionRegistry<>(key, definitions, sources);
    }
}
