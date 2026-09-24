package dev.worldrpg.api.registry;

import dev.worldrpg.api.validation.ValidationReport;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the currently active immutable registry snapshot.
 *
 * <p>Invalid candidates are never published, preserving the last known-good state.</p>
 */
public final class RegistryPublisher {
    private final AtomicReference<RegistrySnapshot> active =
            new AtomicReference<>(RegistrySnapshot.empty());

    public RegistrySnapshot active() {
        return active.get();
    }

    public boolean publishIfValid(RegistrySnapshot candidate, ValidationReport report) {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(report, "report");

        if (report.hasErrors()) {
            return false;
        }

        active.set(candidate);
        return true;
    }
}
