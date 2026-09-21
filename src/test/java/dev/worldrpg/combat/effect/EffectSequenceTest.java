package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectSequenceTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/test_mana");

    @Test
    void effectsApplyInDeclaredOrderAndEmitOrderedEvents() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 50.0);

        EffectSequence sequence = new EffectSequence(List.of(
                new ResourceDrainEffect(EffectRecipient.TARGET, MANA, 20.0),
                new ResourceGainEffect(EffectRecipient.TARGET, MANA, 5.0)
        ));

        EffectSequenceResult result = sequence.execute(
                new EffectContext(source, target, 42)
        );

        assertTrue(result.applied());
        assertEquals(35.0, target.resources().require(MANA).current());
        assertEquals(2, result.events().size());
        assertEquals(42, result.events().get(0).gameTick());
        assertEquals(42, result.events().get(1).gameTick());
    }

    @Test
    void failedValidationPreventsAllMutation() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 50.0);

        CombatEffect alwaysFails = new CombatEffect() {
            @Override
            public ConditionResult validate(EffectContext context) {
                return ConditionResult.fail(
                        RpgId.parse("world_rpg:condition/test_failure"),
                        "fixture failure"
                );
            }

            @Override
            public List<CombatEvent> apply(EffectContext context) {
                throw new AssertionError("apply must not be reached");
            }
        };

        EffectSequence sequence = new EffectSequence(List.of(
                new ResourceDrainEffect(EffectRecipient.TARGET, MANA, 20.0),
                alwaysFails
        ));

        EffectSequenceResult result = sequence.execute(
                new EffectContext(source, target, 7)
        );

        assertFalse(result.applied());
        assertEquals(50.0, target.resources().require(MANA).current());
        assertTrue(result.events().isEmpty());
    }

    @Test
    void missingPoolFailsBeforeEarlierEffectsCanMutate() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 50.0);
        ResourceKey rage = ResourceKey.of("world_rpg:resource/test_rage");

        EffectSequence sequence = new EffectSequence(List.of(
                new ResourceDrainEffect(EffectRecipient.TARGET, MANA, 20.0),
                new ResourceGainEffect(EffectRecipient.TARGET, rage, 10.0)
        ));

        EffectSequenceResult result = sequence.execute(
                new EffectContext(source, target, 8)
        );

        assertFalse(result.applied());
        assertEquals(50.0, target.resources().require(MANA).current());
    }

    private static CombatActor actor(long id, double mana) {
        CombatActor actor = new CombatActor(new CombatActorId(id));
        actor.resources().add(MANA, 100.0, mana);
        return actor;
    }
}
