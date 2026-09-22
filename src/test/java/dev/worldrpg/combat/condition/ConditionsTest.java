package dev.worldrpg.combat.condition;

import dev.worldrpg.api.id.RpgId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionsTest {
    private static final RpgId TOO_LOW =
            RpgId.parse("world_rpg:condition/test_too_low");
    private static final RpgId TOO_HIGH =
            RpgId.parse("world_rpg:condition/test_too_high");

    @Test
    void allCollectsFailures() {
        Condition<Integer> atLeastTen = value -> value >= 10
                ? ConditionResult.pass()
                : ConditionResult.fail(TOO_LOW, "value is below ten");

        Condition<Integer> atMostFive = value -> value <= 5
                ? ConditionResult.pass()
                : ConditionResult.fail(TOO_HIGH, "value is above five");

        ConditionResult result = Conditions.all(atLeastTen, atMostFive).evaluate(7);

        assertFalse(result.passed());
        assertEquals(2, result.failures().size());
    }

    @Test
    void anyPassesWhenOneBranchPasses() {
        Condition<Integer> negative = value -> value < 0
                ? ConditionResult.pass()
                : ConditionResult.fail(TOO_HIGH, "not negative");

        Condition<Integer> even = value -> value % 2 == 0
                ? ConditionResult.pass()
                : ConditionResult.fail(TOO_LOW, "not even");

        assertTrue(Conditions.any(negative, even).evaluate(4).passed());
    }

    @Test
    void emptyAnyFailsRatherThanVacuouslyPassing() {
        ConditionResult result = Conditions.<Integer>any().evaluate(4);
        assertFalse(result.passed());
        assertEquals(1, result.failures().size());
    }

    @Test
    void notInvertsPassState() {
        Condition<Integer> positive = value -> value > 0
                ? ConditionResult.pass()
                : ConditionResult.fail(TOO_LOW, "not positive");

        assertFalse(Conditions.not(positive).evaluate(1).passed());
        assertTrue(Conditions.not(positive).evaluate(-1).passed());
    }
}
