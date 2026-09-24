package dev.worldrpg.content.source;

import dev.worldrpg.api.validation.SourceRef;

import java.util.Objects;

/**
 * One UTF-8 authored content resource before decoding.
 *
 * <p>The logical path is provenance and organization only. Definition identity
 * comes from the document's explicit stable ID.</p>
 */
public record ContentSource(String logicalPath, String content, SourceRef sourceRef) {
    public ContentSource {
        Objects.requireNonNull(logicalPath, "logicalPath");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(sourceRef, "sourceRef");

        if (logicalPath.isBlank()) {
            throw new IllegalArgumentException("logicalPath must not be blank");
        }
    }

    public static ContentSource of(String logicalPath, String content) {
        return new ContentSource(logicalPath, content, SourceRef.of(logicalPath));
    }
}
