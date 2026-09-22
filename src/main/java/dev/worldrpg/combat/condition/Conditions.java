package dev.worldrpg.combat.condition;

import dev.worldrpg.api.id.RpgId;

import java.util.List;
import java.util.Objects;

public final class Conditions {
    private static final RpgId NOT_FAILED =
            RpgId.parse("world_rpg:condition/not_failed");
    private static final RpgId EMPTY_ANY =
            RpgId.parse("world_rpg:condition/no_alternatives");

    private Conditions() {
    }

    @SafeVarargs
    public static <C> Condition<C> all(Condition<C>... conditions) {
        return all(List.of(conditions));
    }

    public static <C> Condition<C> all(
            List<? extends Condition<C>> conditions
    ) {
        List<? extends Condition<C>> immutable =
                List.copyOf(Objects.requireNonNull(conditions, "conditions"));

        return context -> {
            ConditionResult result = ConditionResult.pass();

            for (Condition<C> condition : immutable) {
                result = result.plus(
                        Objects.requireNonNull(condition, "condition")
                                .evaluate(context)
                );
            }

            return result;
        };
    }

    @SafeVarargs
    public static <C> Condition<C> any(Condition<C>... conditions) {
        return any(List.of(conditions));
    }

    public static <C> Condition<C> any(
            List<? extends Condition<C>> conditions
    ) {
        List<? extends Condition<C>> immutable =
                List.copyOf(Objects.requireNonNull(conditions, "conditions"));

        return context -> {
            if (immutable.isEmpty()) {
                return ConditionResult.fail(
                        EMPTY_ANY,
                        "No alternative conditions were provided"
                );
            }

            ConditionResult failures = ConditionResult.pass();

            for (Condition<C> condition : immutable) {
                ConditionResult result =
                        Objects.requireNonNull(condition, "condition")
                                .evaluate(context);

                if (result.passed()) {
                    return ConditionResult.pass();
                }

                failures = failures.plus(result);
            }

            return failures;
        };
    }

    public static <C> Condition<C> not(Condition<C> condition) {
        Objects.requireNonNull(condition, "condition");

        return context -> condition.evaluate(context).passed()
                ? ConditionResult.fail(
                        NOT_FAILED,
                        "Negated condition unexpectedly passed"
                )
                : ConditionResult.pass();
    }
}
