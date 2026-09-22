package dev.worldrpg.combat.reaction;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatEventEnvelope;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombatEventDispatcherTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/test_dispatch_mana");

    @Test
    void listenersRunByPriorityThenRegistrationAndChildrenAreBreadthFirst() {
        CombatEventDispatcher dispatcher = new CombatEventDispatcher(4, 20);

        dispatcher.register(listener(
                "world_rpg:listener/late",
                20,
                new EmitReaction(20)
        ));
        dispatcher.register(listener(
                "world_rpg:listener/early_a",
                10,
                new EmitReaction(10)
        ));
        dispatcher.register(listener(
                "world_rpg:listener/early_b",
                10,
                new EmitReaction(11)
        ));

        List<Integer> executed = new ArrayList<>();

        CombatDispatchResult result = dispatcher.dispatch(
                List.of(event(1)),
                (reaction, cause) -> {
                    EmitReaction emitted = (EmitReaction) reaction;
                    executed.add(emitted.marker());

                    if (cause.depth() == 0) {
                        return List.of(event(emitted.marker()));
                    }

                    return List.of();
                }
        );

        assertEquals(List.of(10, 11, 20, 10, 11, 20, 10, 11, 20, 10, 11, 20), executed);
        assertEquals(4, result.eventCount());

        assertEquals(0, result.trace().get(0).depth());
        assertEquals(1, result.trace().get(1).depth());
        assertEquals(1, result.trace().get(2).depth());
        assertEquals(1, result.trace().get(3).depth());

        assertEquals(1, result.trace().get(1).parentSequence().orElseThrow());
        assertEquals(1, result.trace().get(2).parentSequence().orElseThrow());
        assertEquals(1, result.trace().get(3).parentSequence().orElseThrow());

        assertEquals(
                List.of(
                        RpgId.parse("world_rpg:listener/early_a"),
                        RpgId.parse("world_rpg:listener/early_b"),
                        RpgId.parse("world_rpg:listener/late")
                ),
                dispatcher.listenerIdsInOrder()
        );
    }

    @Test
    void duplicateListenerIdsAreRejected() {
        CombatEventDispatcher dispatcher = new CombatEventDispatcher(4, 20);

        dispatcher.register(listener(
                "world_rpg:listener/same",
                0,
                new EmitReaction(1)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatcher.register(listener(
                        "world_rpg:listener/same",
                        10,
                        new EmitReaction(2)
                ))
        );
    }

    @Test
    void depthGuardStopsRecursiveReactionBeforeExecutingDeeperMutation() {
        CombatEventDispatcher dispatcher = new CombatEventDispatcher(1, 20);

        dispatcher.register(listener(
                "world_rpg:listener/recursive",
                0,
                new EmitReaction(1)
        ));

        List<Integer> executions = new ArrayList<>();

        assertThrows(
                CombatDispatchLimitException.class,
                () -> dispatcher.dispatch(
                        List.of(event(1)),
                        (reaction, cause) -> {
                            executions.add(cause.depth());
                            return List.of(event(cause.depth() + 2));
                        }
                )
        );

        assertEquals(List.of(0), executions);
    }

    @Test
    void eventCountGuardStopsUnboundedFanout() {
        CombatEventDispatcher dispatcher = new CombatEventDispatcher(4, 3);

        dispatcher.register(listener(
                "world_rpg:listener/fanout",
                0,
                new EmitReaction(1)
        ));

        assertThrows(
                CombatDispatchLimitException.class,
                () -> dispatcher.dispatch(
                        List.of(event(1)),
                        (reaction, cause) -> List.of(
                                event(2),
                                event(3),
                                event(4)
                        )
                )
        );
    }

    private static CombatEventListener listener(
            String id,
            int priority,
            CombatReaction reaction
    ) {
        return new CombatEventListener() {
            @Override
            public RpgId id() {
                return RpgId.parse(id);
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public boolean supports(CombatEventEnvelope event) {
                return true;
            }

            @Override
            public List<CombatReaction> react(CombatEventEnvelope event) {
                return List.of(reaction);
            }
        };
    }

    private static CombatEvent event(double marker) {
        return new ResourceChangedEvent(
                0,
                new CombatActorId(1),
                MANA,
                new ResourceChange(marker, marker, 0, 0, true)
        );
    }

    private record EmitReaction(int marker) implements CombatReaction {
    }
}
