package dev.worldrpg.combat.ability;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.effect.EffectSequence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbilityMovementPolicyTest {
    @Test
    void compatibilityConstructorPreservesStationaryCastBehavior() {
        AbilityDefinition ability = new AbilityDefinition(
                RpgId.parse("world_rpg:ability/test/stationary"),
                AbilityCastKind.TIMED,
                20,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                context -> ConditionResult.pass(),
                new EffectSequence(List.of())
        );

        assertEquals(
                AbilityMovementPolicy.INTERRUPT,
                ability.movementPolicy()
        );
    }

    @Test
    void abilityCanExplicitlyAllowMovement() {
        AbilityDefinition ability = new AbilityDefinition(
                RpgId.parse("world_rpg:ability/test/mobile"),
                AbilityCastKind.TIMED,
                20,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                context -> ConditionResult.pass(),
                new EffectSequence(List.of()),
                AbilityMovementPolicy.ALLOW
        );

        assertEquals(
                AbilityMovementPolicy.ALLOW,
                ability.movementPolicy()
        );
    }
}
