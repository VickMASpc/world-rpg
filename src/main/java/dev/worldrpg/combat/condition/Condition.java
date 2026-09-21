package dev.worldrpg.combat.condition;

@FunctionalInterface
public interface Condition<C> {
    ConditionResult evaluate(C context);
}
