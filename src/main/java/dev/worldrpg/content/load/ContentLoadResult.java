package dev.worldrpg.content.load;

import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Objects;

public record ContentLoadResult(
        RegistrySnapshot candidate,
        ValidationReport report,
        boolean published
) {
    public ContentLoadResult {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(report, "report");
    }
}
