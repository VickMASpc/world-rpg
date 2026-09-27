package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastControllerValidationTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void validationIsPureAndMatchesActivationPreflight() {
        CombatActor owner =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        owner.resources().add(
                MANA,
                20.0,
                20.0
        );

        CastController controller =
                new CastController(owner);

        AbilityDefinition ability =
                ability(
                        "world_rpg:ability/test/validate",
                        10.0
                );

        var validation =
                controller.validateActivation(
                        ability,
                        target,
                        0
                );

        assertTrue(validation.passed());
        assertEquals(
                20.0,
                owner.resources()
                        .require(MANA)
                        .current(),
                0.0
        );
        assertTrue(controller.activeCast().isEmpty());
        assertTrue(
                controller.cooldowns()
                        .activeAt(0)
                        .isEmpty()
        );

        var activation =
                controller.tryActivate(
                        ability,
                        target,
                        0
                );

        assertTrue(activation.accepted());
        assertEquals(
                10.0,
                owner.resources()
                        .require(MANA)
                        .current(),
                0.0
        );
        assertTrue(controller.activeCast().isPresent());
    }

    private static AbilityDefinition ability(
            String id,
            double manaCost
    ) {
        return new AbilityDefinition(
                RpgId.parse(id),
                AbilityCastKind.TIMED,
                20,
                OptionalLong.empty(),
                0,
                0,
                List.of(
                        new AbilityCost(
                                MANA,
                                manaCost
                        )
                ),
                Conditions.all(List.of()),
                new EffectSequence(List.of())
        );
    }
}
