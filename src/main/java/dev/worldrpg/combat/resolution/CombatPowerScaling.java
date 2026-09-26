package dev.worldrpg.combat.resolution;

import java.util.List;
import java.util.Objects;

/**
 * Power-scaling contract carried by a magnitude request.
 *
 * <p>New production content should use explicit terms. LEGACY_PROFILE exists
 * only so older P3/P4 fixtures can retain their original school-based scaling
 * while they are migrated.</p>
 */
public record CombatPowerScaling(
        Mode mode,
        List<CombatPowerTerm> terms
) {
    public enum Mode {
        EXPLICIT,
        LEGACY_PROFILE
    }

    public CombatPowerScaling {
        Objects.requireNonNull(mode, "mode");
        terms = List.copyOf(
                Objects.requireNonNull(terms, "terms")
        );

        if (mode == Mode.LEGACY_PROFILE && !terms.isEmpty()) {
            throw new IllegalArgumentException(
                    "legacy profile scaling cannot carry explicit terms"
            );
        }
    }

    public static CombatPowerScaling none() {
        return explicit(List.of());
    }

    public static CombatPowerScaling explicit(
            CombatPowerTerm... terms
    ) {
        return explicit(List.of(terms));
    }

    public static CombatPowerScaling explicit(
            List<CombatPowerTerm> terms
    ) {
        return new CombatPowerScaling(
                Mode.EXPLICIT,
                terms
        );
    }

    public static CombatPowerScaling legacyProfile() {
        return new CombatPowerScaling(
                Mode.LEGACY_PROFILE,
                List.of()
        );
    }
}
