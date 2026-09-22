package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.event.CastCompletedEvent;
import dev.worldrpg.combat.event.CastInterruptedEvent;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastControllerTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/test_mana");

    @Test
    void aggregatedCostsFailBeforeAnyResourceIsSpent() {
        CombatActor caster = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);
        CastController controller = new CastController(caster);

        AbilityDefinition ability = ability(
                "world_rpg:ability/test/too_expensive",
                AbilityCastKind.INSTANT,
                0,
                OptionalLong.empty(),
                List.of(
                        new AbilityCost(MANA, 60.0),
                        new AbilityCost(MANA, 50.0)
                ),
                new EffectSequence(List.of())
        );

        AbilityActivationResult result =
                controller.tryActivate(ability, target, 0);

        assertFalse(result.accepted());
        assertEquals(100.0, caster.resources().require(MANA).current());
        assertTrue(result.events().isEmpty());
    }

    @Test
    void timedCastSpendsAtStartAndResolvesOnlyAtCompletion() {
        CombatActor caster = actor(1, 100.0);
        CombatActor target = actor(2, 50.0);
        CastController controller = new CastController(caster);

        AbilityDefinition ability = ability(
                "world_rpg:ability/test/timed",
                AbilityCastKind.TIMED,
                10,
                OptionalLong.empty(),
                List.of(new AbilityCost(MANA, 20.0)),
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                MANA,
                                10.0
                        )
                ))
        );

        AbilityActivationResult activation =
                controller.tryActivate(ability, target, 0);

        assertTrue(activation.accepted());
        assertTrue(activation.castId().isPresent());
        assertEquals(80.0, caster.resources().require(MANA).current());
        assertEquals(50.0, target.resources().require(MANA).current());

        assertTrue(controller.tick(9).isEmpty());
        assertEquals(50.0, target.resources().require(MANA).current());

        var completionEvents = controller.tick(10);

        assertEquals(40.0, target.resources().require(MANA).current());
        assertTrue(completionEvents.get(0) instanceof CastCompletedEvent);
        assertTrue(controller.activeCast().isEmpty());
    }

    @Test
    void interruptedCastDoesNotRefundOrApplyEffects() {
        CombatActor caster = actor(1, 100.0);
        CombatActor target = actor(2, 50.0);
        CastController controller = new CastController(caster);

        AbilityDefinition ability = ability(
                "world_rpg:ability/test/interrupted",
                AbilityCastKind.TIMED,
                20,
                OptionalLong.empty(),
                List.of(new AbilityCost(MANA, 25.0)),
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                MANA,
                                20.0
                        )
                ))
        );

        assertTrue(controller.tryActivate(ability, target, 0).accepted());

        CastInterruptedEvent interrupted =
                controller.interrupt(CastInterruptionReason.MOVEMENT, 5)
                        .orElseThrow();

        assertEquals(CastInterruptionReason.MOVEMENT, interrupted.reason());
        assertEquals(75.0, caster.resources().require(MANA).current());
        assertEquals(50.0, target.resources().require(MANA).current());
        assertTrue(controller.activeCast().isEmpty());
    }

    @Test
    void channelAppliesAtDeterministicIntervals() {
        CombatActor caster = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);
        CastController controller = new CastController(caster);

        AbilityDefinition ability = ability(
                "world_rpg:ability/test/channel",
                AbilityCastKind.CHANNEL,
                12,
                OptionalLong.of(4),
                List.of(),
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                MANA,
                                5.0
                        )
                ))
        );

        assertTrue(controller.tryActivate(ability, target, 100).accepted());

        assertTrue(controller.tick(103).isEmpty());
        controller.tick(104);
        assertEquals(95.0, target.resources().require(MANA).current());

        controller.tick(112);
        assertEquals(85.0, target.resources().require(MANA).current());
        assertTrue(controller.activeCast().isEmpty());
    }

    private static CombatActor actor(long id, double mana) {
        CombatActor actor = new CombatActor(new CombatActorId(id));
        actor.resources().add(MANA, 100.0, mana);
        return actor;
    }

    private static AbilityDefinition ability(
            String id,
            AbilityCastKind kind,
            long duration,
            OptionalLong interval,
            List<AbilityCost> costs,
            EffectSequence effects
    ) {
        return new AbilityDefinition(
                RpgId.parse(id),
                kind,
                duration,
                interval,
                30,
                5,
                costs,
                context -> ConditionResult.pass(),
                effects
        );
    }
}
