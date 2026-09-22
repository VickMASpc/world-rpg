package dev.worldrpg.combat.reaction;

import dev.worldrpg.combat.event.CombatEventEnvelope;

import java.util.List;
import java.util.Objects;

public record CombatDispatchResult(List<CombatEventEnvelope> trace) {
    public CombatDispatchResult {
        trace = List.copyOf(Objects.requireNonNull(trace, "trace"));
    }

    public int eventCount() {
        return trace.size();
    }
}
