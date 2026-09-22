package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.target.TargetConditions;
import dev.worldrpg.combat.target.TargetObservation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastObservationRevalidationTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/test_target_health");

    @Test
    void timedCastReobservesTargetAtCompletion() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));
        target.resources().add(HEALTH, 100.0, 100.0);

        AtomicReference<TargetObservation> observation =
                new AtomicReference<>(validObservation());

        AbilityObservationProvider provider =
                (ignoredSource, ignoredTarget) -> observation.get();

        CastController controller = new CastController(
                source,
                new CooldownBook(),
                provider
        );

        AbilityDefinition ability = new AbilityDefinition(
                RpgId.parse("world_rpg:ability/test/revalidate"),
                AbilityCastKind.TIMED,
                10,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                Conditions.all(
                        TargetConditions.requireSourceAlive(),
                        TargetConditions.requireTargetAlive(),
                        TargetConditions.requireSameWorld(),
                        TargetConditions.requireLineOfSight(),
                        TargetConditions.maxRange(10.0)
                ),
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                HEALTH,
                                25.0
                        )
                ))
        );

        assertTrue(controller.tryActivate(ability, target, 0).accepted());

        observation.set(TargetObservation.observed(
                false,
                true,
                true,
                true,
                false,
                OptionalDouble.of(25.0)
        ));

        var events = controller.tick(10);

        assertEquals(100.0, target.resources().require(HEALTH).current());
        assertTrue(events.get(0) instanceof dev.worldrpg.combat.event.CastInterruptedEvent);
        assertTrue(controller.activeCast().isEmpty());
    }

    private static TargetObservation validObservation() {
        return TargetObservation.observed(
                false,
                true,
                true,
                true,
                true,
                OptionalDouble.of(25.0)
        );
    }
}
