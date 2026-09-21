package dev.worldrpg.content.source;

import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic set of authored sources.
 */
public final class ContentSourceSet {
    private final List<ContentSource> sources;

    private ContentSourceSet(List<ContentSource> sources) {
        this.sources = List.copyOf(sources);
    }

    public static ContentSourceSet create(
            Iterable<ContentSource> input,
            ValidationReport report
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(report, "report");

        Map<String, ContentSource> unique = new LinkedHashMap<>();

        for (ContentSource source : input) {
            Objects.requireNonNull(source, "source");

            ContentSource previous = unique.putIfAbsent(source.logicalPath(), source);
            if (previous != null) {
                report.error(
                        "source.duplicate_path",
                        "Duplicate logical content path; first source was " + previous.sourceRef().display(),
                        source.sourceRef()
                );
            }
        }

        List<ContentSource> sorted = new ArrayList<>(unique.values());
        sorted.sort(Comparator.comparing(ContentSource::logicalPath));
        return new ContentSourceSet(sorted);
    }

    public List<ContentSource> sources() {
        return sources;
    }

    public int size() {
        return sources.size();
    }

    public static SourceRef internalSource() {
        return SourceRef.of("<content-source-set>");
    }
}
