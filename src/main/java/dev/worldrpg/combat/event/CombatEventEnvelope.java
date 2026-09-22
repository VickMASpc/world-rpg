package dev.worldrpg.combat.event;

import java.util.Objects;
import java.util.OptionalLong;

/**
 * Immutable causal metadata wrapped around one emitted combat event.
 */
public record CombatEventEnvelope(
        long sequence,
        int depth,
        OptionalLong parentSequence,
        CombatEvent event
) {
    public CombatEventEnvelope {
        if (sequence < 1) {
            throw new IllegalArgumentException("event sequence must be >= 1");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("event depth must be >= 0");
        }
        Objects.requireNonNull(parentSequence, "parentSequence");
        Objects.requireNonNull(event, "event");

        if (depth == 0 && parentSequence.isPresent()) {
            throw new IllegalArgumentException("root event cannot have a parent");
        }
        if (depth > 0 && parentSequence.isEmpty()) {
            throw new IllegalArgumentException("child event must have a parent");
        }
    }
}
