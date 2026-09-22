package dev.worldrpg.combat.ability;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.effect.EffectSequence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AbilityDefinitionTest {
    @Test
    void validatesCastShape() {
        new AbilityDefinition(
                RpgId.parse("world_rpg:ability/test/instant"),
                AbilityCastKind.INSTANT,
                0,
                OptionalLong.empty(),
                0,
                0,
                List.of(),
                context -> ConditionResult.pass(),
                new EffectSequence(List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new AbilityDefinition(
                        RpgId.parse("world_rpg:ability/test/bad_channel"),
                        AbilityCastKind.CHANNEL,
                        20,
                        OptionalLong.of(30),
                        0,
                        0,
                        List.of(),
                        context -> ConditionResult.pass(),
                        new EffectSequence(List.of())
                )
        );
    }
}
