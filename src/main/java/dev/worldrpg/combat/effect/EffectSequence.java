package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ordered effect execution.
 *
 * <p>All effect preconditions are evaluated before any effect mutates state.
 * P3 does not provide rollback for programmer errors thrown during apply.</p>
 */
public final class EffectSequence {
    private final List<CombatEffect> effects;

    public EffectSequence(List<CombatEffect> effects) {
        this.effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
    }

    public EffectSequenceResult execute(EffectContext context) {
        Objects.requireNonNull(context, "context");

        ConditionResult validation = ConditionResult.pass();

        for (CombatEffect effect : effects) {
            validation = validation.plus(
                    Objects.requireNonNull(effect, "effect").validate(context)
            );
        }

        if (!validation.passed()) {
            return new EffectSequenceResult(validation, List.of());
        }

        List<CombatEvent> events = new ArrayList<>();
        for (CombatEffect effect : effects) {
            events.addAll(effect.apply(context));
        }

        return new EffectSequenceResult(ConditionResult.pass(), events);
    }

    public List<CombatEffect> effects() {
        return effects;
    }
}
