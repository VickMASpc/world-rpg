package dev.worldrpg.combat.resolution;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;

import java.util.Objects;

/**
 * Short-lived server-side request handed from generic P3 effects into the P4
 * mathematics layer.
 *
 * <p>The authored base magnitude is an input, not a resolved gameplay result.
 * P4 owns hit/crit/mitigation/scaling/absorb/final-amount decisions.</p>
 */
public record CombatMagnitudeRequest(
        long gameTick,
        CombatMagnitudeKind kind,
        CombatActor source,
        CombatActor target,
        RpgId causeId,
        RpgId schoolId,
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

        if (!Double.isFinite(authoredBaseMagnitude)
                || authoredBaseMagnitude < 0.0) {
            throw new IllegalArgumentException(
                    "authoredBaseMagnitude must be finite and >= 0"
            );
        }
    }
}
