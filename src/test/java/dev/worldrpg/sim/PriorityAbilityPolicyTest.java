package dev.worldrpg.sim;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityAbilityPolicyTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void choosesFirstCurrentlyLegalAbilityWithoutMutatingState() {
        CombatActor owner =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        owner.resources().add(
                MANA,
                5.0,
                5.0
        );

        AbilityDefinition expensive =
                ability(
                        "world_rpg:ability/test/expensive",
                        10.0
                );
        AbilityDefinition fallback =
                ability(
                        "world_rpg:ability/test/fallback",
                        0.0
                );

        CastController controller =
                new CastController(owner);

        PriorityAbilityPolicy policy =
                new PriorityAbilityPolicy(
                        List.of(
                                expensive,
                                fallback
                        )
                );

        AbilityDefinition chosen =
                policy.choose(
                        controller,
                        target,
                        0
                ).orElseThrow();

        assertEquals(
                fallback.id(),
                chosen.id()
        );
        assertEquals(
                5.0,
                owner.resources()
                        .require(MANA)
                        .current(),
                0.0
        );
        assertTrue(controller.activeCast().isEmpty());
    }

    @Test
    void returnsEmptyWhileOwnerIsAlreadyCasting() {
        CombatActor owner =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        owner.resources().add(
                MANA,
                10.0,
                10.0
        );

        AbilityDefinition cast =
                ability(
                        "world_rpg:ability/test/cast",
                        0.0
                );

        CastController controller =
                new CastController(owner);

        assertTrue(
                controller.tryActivate(
                        cast,
                        target,
                        0
                ).accepted()
        );

        PriorityAbilityPolicy policy =
                new PriorityAbilityPolicy(
                        List.of(cast)
                );

        assertTrue(
                policy.choose(
                        controller,
                        target,
                        1
                ).isEmpty()
        );
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
                manaCost == 0.0
                        ? List.of()
                        : List.of(
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
