package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityInterruptionPolicy;
import dev.worldrpg.combat.ability.AbilityMovementPolicy;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.cast.LookupCombatCastControlGateway;
import dev.worldrpg.combat.condition.Conditions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterruptEffectTest {
    @Test
    void effectInterruptsTargetProductionCast() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        CastController targetCasts =
                new CastController(target);

        AbilityDefinition cast =
                timedAbility(
                        "world_rpg:ability/test/interruptible",
                        AbilityInterruptionPolicy.INTERRUPTIBLE
                );

        assertTrue(
                targetCasts.tryActivate(
                        cast,
                        source,
                        0
                ).accepted()
        );

        var gateway =
                gatewayFor(target, targetCasts);

        InterruptEffect effect =
                new InterruptEffect(
                        EffectRecipient.TARGET,
                        gateway
                );

        EffectSequenceResult result =
                new EffectSequence(
                        List.of(effect)
                ).execute(
                        new EffectContext(
                                source,
                                target,
                                10
                        )
                );

        assertTrue(result.applied());
        assertEquals(1, result.events().size());
        assertTrue(targetCasts.activeCast().isEmpty());
        assertTrue(
                result.events().get(0)
                        instanceof dev.worldrpg.combat.event.CastInterruptedEvent
        );
    }

    @Test
    void uninterruptibleCastFailsPreflightAndKeepsCasting() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        CastController targetCasts =
                new CastController(target);

        AbilityDefinition cast =
                timedAbility(
                        "world_rpg:ability/test/uninterruptible",
                        AbilityInterruptionPolicy.UNINTERRUPTIBLE
                );

        assertTrue(
                targetCasts.tryActivate(
                        cast,
                        source,
                        0
                ).accepted()
        );

        InterruptEffect effect =
                new InterruptEffect(
                        EffectRecipient.TARGET,
                        gatewayFor(target, targetCasts)
                );

        EffectSequenceResult result =
                new EffectSequence(
                        List.of(effect)
                ).execute(
                        new EffectContext(
                                source,
                                target,
                                10
                        )
                );

        assertFalse(result.applied());
        assertTrue(targetCasts.activeCast().isPresent());
        assertTrue(
                result.validation()
                        .failures()
                        .stream()
                        .anyMatch(failure ->
                                failure.code().equals(
                                        RpgId.parse(
                                                "world_rpg:condition/cast_uninterruptible"
                                        )
                                )
                        )
        );
    }

    @Test
    void notCastingFailsPreflightWithoutMutation() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        CastController targetCasts =
                new CastController(target);

        InterruptEffect effect =
                new InterruptEffect(
                        EffectRecipient.TARGET,
                        gatewayFor(target, targetCasts)
                );

        EffectSequenceResult result =
                new EffectSequence(
                        List.of(effect)
                ).execute(
                        new EffectContext(
                                source,
                                target,
                                10
                        )
                );

        assertFalse(result.applied());
        assertEquals(0, result.events().size());
    }

    private static LookupCombatCastControlGateway gatewayFor(
            CombatActor actor,
            CastController controller
    ) {
        Map<CombatActorId, CastController> controllers =
                Map.of(actor.id(), controller);

        return new LookupCombatCastControlGateway(
                id -> Optional.ofNullable(
                        controllers.get(id)
                )
        );
    }

    private static AbilityDefinition timedAbility(
            String id,
            AbilityInterruptionPolicy interruptionPolicy
    ) {
        return new AbilityDefinition(
                RpgId.parse(id),
                AbilityCastKind.TIMED,
                80,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                Conditions.all(List.of()),
                new EffectSequence(List.of()),
                AbilityMovementPolicy.ALLOW,
                interruptionPolicy
        );
    }
}
