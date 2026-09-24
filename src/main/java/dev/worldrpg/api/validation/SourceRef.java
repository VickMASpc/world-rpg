package dev.worldrpg.api.validation;

import java.util.Objects;

/**
 * Human-facing location of authored source data.
 *
 * <p>Line and column are one-based when known. Zero means unknown.</p>
 */
public record SourceRef(String source, int line, int column) {
    public SourceRef {
        Objects.requireNonNull(source, "source");
        if (source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        if (line < 0 || column < 0) {
            throw new IllegalArgumentException("line and column must be >= 0");
        }
    }

    public static SourceRef of(String source) {
        return new SourceRef(source, 0, 0);
    }

    public String display() {
        if (line == 0) {
            return source;
        }
        if (column == 0) {
            return source + ":" + line;
        }
        return source + ":" + line + ":" + column;
    }
}
