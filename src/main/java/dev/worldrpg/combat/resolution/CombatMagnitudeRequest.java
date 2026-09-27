package dev.worldrpg.combat.resolution;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;

/**
 * Short-lived server-side request handed from generic P3 effects into the P4
 * mathematics layer.
 *
 * <p>The authored base magnitude and explicit power terms are inputs, not
 * resolved gameplay results. P4 owns contact/crit/mitigation/absorb/final
 * amount decisions.</p>
 */
public record CombatMagnitudeRequest(
        long gameTick,
        CombatMagnitudeKind kind,
        CombatActor source,
        CombatActor target,
        RpgId causeId,
        RpgId schoolId,
        RpgId resolutionProfileId,
        CombatPowerScaling powerScaling,
        double authoredBaseMagnitude
) {
    public CombatMagnitudeRequest {
        if (gameTick < 0) {
            throw new IllegalArgumentException(
                    "gameTick must be >= 0"
            );
        }

        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(causeId, "causeId");
        Objects.requireNonNull(schoolId, "schoolId");
        Objects.requireNonNull(
                resolutionProfileId,
                "resolutionProfileId"
        );
        Objects.requireNonNull(
                powerScaling,
                "powerScaling"
        );

        if (!Double.isFinite(authoredBaseMagnitude)
                || authoredBaseMagnitude < 0.0) {
            throw new IllegalArgumentException(
                    "authoredBaseMagnitude must be finite and >= 0"
            );
        }
    }

    /**
     * Compatibility constructor for pre-explicit-scaling callers.
     *
     * <p>Legacy scaling is isolated behind an explicit compatibility mode.
     * New production effects should supply CombatPowerScaling directly.</p>
     */
    public CombatMagnitudeRequest(
            long gameTick,
            CombatMagnitudeKind kind,
            CombatActor source,
            CombatActor target,
            RpgId causeId,
            RpgId schoolId,
            RpgId resolutionProfileId,
            double authoredBaseMagnitude
    ) {
        this(
                gameTick,
                kind,
                source,
                target,
                causeId,
                schoolId,
                resolutionProfileId,
                CombatPowerScaling.legacyProfile(),
                authoredBaseMagnitude
        );
    }

    /**
     * Compatibility constructor for pre-profile callers.
     */
    public CombatMagnitudeRequest(
            long gameTick,
            CombatMagnitudeKind kind,
            CombatActor source,
            CombatActor target,
            RpgId causeId,
            RpgId schoolId,
            double authoredBaseMagnitude
    ) {
        this(
                gameTick,
                kind,
                source,
                target,
                causeId,
                schoolId,
                CombatResolutionProfileIds.GUARANTEED,
                CombatPowerScaling.legacyProfile(),
                authoredBaseMagnitude
        );
    }
}
