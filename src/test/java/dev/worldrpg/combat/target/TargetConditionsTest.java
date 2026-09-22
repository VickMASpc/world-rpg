package dev.worldrpg.combat.target;

import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetConditionsTest {
    private final CombatActor source =
            new CombatActor(new CombatActorId(1));
    private final CombatActor target =
            new CombatActor(new CombatActorId(2));

    @Test
    void observedFactsDriveTargetRules() {
        AbilityContext valid = context(TargetObservation.observed(
                false,
                true,
                true,
                true,
                true,
                OptionalDouble.of(25.0)
        ));

        assertTrue(TargetConditions.requireSourceAlive().evaluate(valid).passed());
        assertTrue(TargetConditions.requireTargetAlive().evaluate(valid).passed());
        assertTrue(TargetConditions.requireSameWorld().evaluate(valid).passed());
        assertTrue(TargetConditions.disallowSelf().evaluate(valid).passed());
        assertTrue(TargetConditions.requireLineOfSight().evaluate(valid).passed());
        assertTrue(TargetConditions.maxRange(5.0).evaluate(valid).passed());

        AbilityContext tooFar = context(TargetObservation.observed(
                false,
                true,
                true,
                true,
                true,
                OptionalDouble.of(25.0001)
        ));

        assertFalse(TargetConditions.maxRange(5.0).evaluate(tooFar).passed());
    }

    @Test
    void unavailableObservationFailsRulesRatherThanGuessing() {
        AbilityContext unavailable =
                context(TargetObservation.unavailable());

        assertFalse(
                TargetConditions.requireTargetAlive()
                        .evaluate(unavailable)
                        .passed()
        );
        assertFalse(
                TargetConditions.maxRange(30)
                        .evaluate(unavailable)
                        .passed()
        );
    }

    private AbilityContext context(TargetObservation observation) {
        return new AbilityContext(
                source,
                target,
                0,
                observation
        );
    }
}
