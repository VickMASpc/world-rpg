package dev.worldrpg.content.decode;

import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;

import java.util.Objects;

public record DefinitionHeader(
        SchemaVersion schema,
        RpgId registry,
        RpgId id
) {
    public DefinitionHeader {
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
    }
}
