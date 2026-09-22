package dev.worldrpg.combat.target;

import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetFacingConditionTest {
    private final CombatActor source =
            new CombatActor(new CombatActorId(1));
    private final CombatActor target =
            new CombatActor(new CombatActorId(2));

    @Test
    void frontHemisphereUsesDotProductWithoutImposingGlobalFacingRule() {
        var front = context(0.2);
        var behind = context(-0.2);

        assertTrue(
                TargetConditions.requireFacingArcDegrees(180.0)
                        .evaluate(front)
                        .passed()
        );
        assertFalse(
                TargetConditions.requireFacingArcDegrees(180.0)
                        .evaluate(behind)
                        .passed()
        );
    }

    @Test
    void fullCircleAcceptsBehindTarget() {
        assertTrue(
                TargetConditions.requireFacingArcDegrees(360.0)
                        .evaluate(context(-1.0))
                        .passed()
        );
    }

    @Test
    void missingFacingFailsOnlyWhenAbilityRequiresFacing() {
        AbilityContext context = new AbilityContext(
                source,
                target,
                0,
                TargetObservation.observed(
                        false,
                        true,
                        true,
                        true,
                        true,
                        OptionalDouble.of(4.0)
                )
        );

        assertFalse(
                TargetConditions.requireFacingArcDegrees(180.0)
                        .evaluate(context)
                        .passed()
        );
        assertTrue(
                TargetConditions.requireTargetAlive()
                        .evaluate(context)
                        .passed()
        );
    }

    private AbilityContext context(double dot) {
        return new AbilityContext(
                source,
                target,
                0,
                TargetObservation.observed(
                        false,
                        true,
                        true,
                        true,
                        true,
                        OptionalDouble.of(4.0),
                        OptionalDouble.of(dot)
                )
        );
    }
}
