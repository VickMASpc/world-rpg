package dev.worldrpg.combat.event;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.math.CombatResolutionTrace;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;

import java.util.Objects;

/**
 * Semantic damage/healing fact emitted after authoritative resource mutation.
 */
public record CombatMagnitudeResolvedEvent(
        long gameTick,
        CombatMagnitudeKind kind,
        CombatActorId sourceActorId,
        CombatActorId targetActorId,
        RpgId causeId,
        RpgId schoolId,
        CombatResolutionTrace trace
) implements CombatEvent {
    public CombatMagnitudeResolvedEvent {
        if (gameTick < 0) {
            throw new IllegalArgumentException(
                    "gameTick must be >= 0"
            );
        }

        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(sourceActorId, "sourceActorId");
        Objects.requireNonNull(targetActorId, "targetActorId");
        Objects.requireNonNull(causeId, "causeId");
        Objects.requireNonNull(schoolId, "schoolId");
        Objects.requireNonNull(trace, "trace");
    }
}
