package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.CombatMagnitudeEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.CombatSchools;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class P4CastResolutionIntegrationTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void timedP3CastSpendsCostThenResolvesP4DamageAtCompletion() {
        CombatActor caster = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));

        caster.resources().add(MANA, 100.0, 100.0);
        target.resources().add(HEALTH, 100.0, 100.0);

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        new CombatMathProfile(
                                HEALTH,
                                0.0,
                                0.0,
                                0.0,
                                2.0,
                                1.0,
                                100.0,
                                0.75
                        ),
                        () -> 0.5
                );

        RpgId abilityId =
                RpgId.parse(
                        "world_rpg:ability/test/p4_timed_bolt"
                );

        AbilityDefinition ability =
                new AbilityDefinition(
                        abilityId,
                        AbilityCastKind.TIMED,
                        20,
                        OptionalLong.empty(),
                        0,
                        0,
                        List.of(
                                new AbilityCost(
                                        MANA,
                                        20.0
                                )
                        ),
                        Conditions.all(List.of()),
                        new EffectSequence(
                                List.of(
                                        new CombatMagnitudeEffect(
                                                EffectRecipient.TARGET,
                                                CombatMagnitudeKind.DAMAGE,
                                                abilityId,
                                                CombatSchools.ARCANE.id(),
                                                CombatResolutionProfileIds.GUARANTEED,
                                                30.0,
                                                resolver
                                        )
                                )
                        )
                );

        CastController casts = new CastController(caster);

        AbilityActivationResult activation =
                casts.tryActivate(
                        ability,
                        target,
                        0
                );

        assertTrue(activation.accepted());
        assertTrue(casts.activeCast().isPresent());
        assertEquals(
                80.0,
                caster.resources().require(MANA).current(),
                0.0
        );
        assertEquals(
                100.0,
                target.resources().require(HEALTH).current(),
                0.0
        );

        assertTrue(casts.tick(19).isEmpty());
        assertEquals(
                100.0,
                target.resources().require(HEALTH).current(),
                0.0
        );

        var completionEvents = casts.tick(20);

        assertTrue(casts.activeCast().isEmpty());
        assertEquals(
                70.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
        assertTrue(
                completionEvents.stream()
                        .anyMatch(event ->
                                event instanceof dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent
                        )
        );
    }
}
