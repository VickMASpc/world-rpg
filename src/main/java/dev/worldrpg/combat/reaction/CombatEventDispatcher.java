package dev.worldrpg.combat.reaction;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatEventEnvelope;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Set;

/**
 * Deterministic post-event reaction dispatcher.
 *
 * <p>Events are immutable facts. Listeners observe facts and describe reaction
 * requests. Only the executor may perform gameplay mutation.</p>
 *
 * <p>Traversal is breadth-first. All direct children of earlier events are
 * enqueued in deterministic listener/reaction/emission order.</p>
 */
public final class CombatEventDispatcher {
    private static final Comparator<RegisteredListener> LISTENER_ORDER =
            Comparator.comparingInt((RegisteredListener value) -> value.listener().priority())
                    .thenComparingLong(RegisteredListener::registrationOrder);

    private final int maxDepth;
    private final int maxEvents;
    private final List<RegisteredListener> listeners = new ArrayList<>();
    private final Set<RpgId> listenerIds = new HashSet<>();
    private long nextRegistrationOrder = 1L;

    public CombatEventDispatcher(int maxDepth, int maxEvents) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0");
        }
        if (maxEvents < 1) {
            throw new IllegalArgumentException("maxEvents must be >= 1");
        }

        this.maxDepth = maxDepth;
        this.maxEvents = maxEvents;
    }

    public void register(CombatEventListener listener) {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(listener.id(), "listener.id()");

        if (!listenerIds.add(listener.id())) {
            throw new IllegalArgumentException(
                    "Duplicate combat event listener ID: " + listener.id()
            );
        }

        listeners.add(new RegisteredListener(
                listener,
                nextRegistrationOrder++
        ));
        listeners.sort(LISTENER_ORDER);
    }

    public CombatDispatchResult dispatch(
            List<CombatEvent> rootEvents,
            CombatReactionExecutor executor
    ) {
        Objects.requireNonNull(rootEvents, "rootEvents");
        Objects.requireNonNull(executor, "executor");

        Queue<CombatEventEnvelope> queue = new ArrayDeque<>();
        List<CombatEventEnvelope> trace = new ArrayList<>();
        long nextSequence = 1L;

        for (CombatEvent event : rootEvents) {
            requireCapacity(trace.size() + queue.size() + 1);

            queue.add(new CombatEventEnvelope(
                    nextSequence++,
                    0,
                    OptionalLong.empty(),
                    Objects.requireNonNull(event, "root event")
            ));
        }

        while (!queue.isEmpty()) {
            CombatEventEnvelope current = queue.remove();
            trace.add(current);

            List<ReactionSelection> reactions = selectReactions(current);

            if (!reactions.isEmpty() && current.depth() >= maxDepth) {
                throw new CombatDispatchLimitException(
                        "Combat reaction depth exceeded maxDepth=" + maxDepth
                                + " at event sequence " + current.sequence()
                );
            }

            for (ReactionSelection selection : reactions) {
                List<CombatEvent> emitted = List.copyOf(
                        Objects.requireNonNull(
                                executor.execute(selection.reaction(), current),
                                "reaction executor result"
                        )
                );

                requireCapacity(trace.size() + queue.size() + emitted.size());

                for (CombatEvent child : emitted) {
                    queue.add(new CombatEventEnvelope(
                            nextSequence++,
                            current.depth() + 1,
                            OptionalLong.of(current.sequence()),
                            Objects.requireNonNull(child, "emitted child event")
                    ));
                }
            }
        }

        return new CombatDispatchResult(trace);
    }

    public List<RpgId> listenerIdsInOrder() {
        return listeners.stream()
                .map(value -> value.listener().id())
                .toList();
    }

    private List<ReactionSelection> selectReactions(
            CombatEventEnvelope event
    ) {
        List<ReactionSelection> selections = new ArrayList<>();

        for (RegisteredListener registered : listeners) {
            CombatEventListener listener = registered.listener();

            if (!listener.supports(event)) {
                continue;
            }

            List<CombatReaction> reactions = List.copyOf(
                    Objects.requireNonNull(
                            listener.react(event),
                            "listener reaction list"
                    )
            );

            for (CombatReaction reaction : reactions) {
                selections.add(new ReactionSelection(
                        listener.id(),
                        Objects.requireNonNull(reaction, "reaction")
                ));
            }
        }

        return selections;
    }

    private void requireCapacity(int requestedCount) {
        if (requestedCount > maxEvents) {
            throw new CombatDispatchLimitException(
                    "Combat event count exceeded maxEvents=" + maxEvents
                            + "; requested at least " + requestedCount
            );
        }
    }

    private record RegisteredListener(
            CombatEventListener listener,
            long registrationOrder
    ) {
    }

    private record ReactionSelection(
            RpgId listenerId,
            CombatReaction reaction
    ) {
        private ReactionSelection {
            Objects.requireNonNull(listenerId, "listenerId");
            Objects.requireNonNull(reaction, "reaction");
        }
    }
}
