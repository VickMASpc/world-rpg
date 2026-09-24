package dev.worldrpg.content.source;

import dev.worldrpg.api.validation.ValidationReport;

import java.util.List;
import java.util.Objects;

/**
 * Sources plus diagnostics produced before the core content loader begins.
 *
 * <p>Adapter errors are merged into the same candidate-load transaction, so a
 * partial physical read can never publish a partial registry snapshot.</p>
 */
public record ContentSourceBatch(
        List<ContentSource> sources,
        ValidationReport diagnostics
) {
    public ContentSourceBatch {
        sources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public static ContentSourceBatch clean(
            Iterable<ContentSource> sources
    ) {
        Objects.requireNonNull(sources, "sources");

        List<ContentSource> copy = new java.util.ArrayList<>();
        for (ContentSource source : sources) {
            copy.add(Objects.requireNonNull(source, "source"));
        }

        return new ContentSourceBatch(
                copy,
                new ValidationReport()
        );
    }
}
