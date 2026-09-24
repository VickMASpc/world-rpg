package dev.worldrpg.content.decode;

import com.google.gson.JsonObject;
import dev.worldrpg.content.source.ContentSource;

import java.util.Objects;

/**
 * Structurally valid common JSON envelope. Domain decoding happens afterward.
 */
public record DecodedJsonDocument(
        DefinitionHeader header,
        JsonObject root,
        ContentSource source
) {
    public DecodedJsonDocument {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(source, "source");
    }
}
