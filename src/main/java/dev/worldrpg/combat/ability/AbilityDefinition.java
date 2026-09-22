package dev.worldrpg.combat.ability;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.Condition;
import dev.worldrpg.combat.effect.EffectSequence;

import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Compiled runtime ability definition used by the P3 kernel.
 *
 * <p>Authored JSON will eventually decode/compile into this shape. Numeric
 * fixture values here are mechanism inputs, not P4 balance decisions.</p>
 */
public record AbilityDefinition(
        RpgId id,
        AbilityCastKind castKind,
        long castDurationTicks,
        OptionalLong channelIntervalTicks,
        long cooldownTicks,
        long globalCooldownTicks,
        List<AbilityCost> costs,
        Condition<AbilityContext> activationCondition,
        EffectSequence effects,
        AbilityMovementPolicy movementPolicy
) implements RpgDefinition {
    public AbilityDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(castKind, "castKind");
        Objects.requireNonNull(channelIntervalTicks, "channelIntervalTicks");
        costs = List.copyOf(Objects.requireNonNull(costs, "costs"));
        Objects.requireNonNull(activationCondition, "activationCondition");
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(movementPolicy, "movementPolicy");

        if (castDurationTicks < 0 || cooldownTicks < 0 || globalCooldownTicks < 0) {
            throw new IllegalArgumentException("ability tick durations must be >= 0");
        }

        switch (castKind) {
            case INSTANT -> {
                if (castDurationTicks != 0 || channelIntervalTicks.isPresent()) {
                    throw new IllegalArgumentException(
                            "instant ability must have zero duration and no channel interval"
                    );
                }
            }
            case TIMED -> {
                if (castDurationTicks < 1 || channelIntervalTicks.isPresent()) {
                    throw new IllegalArgumentException(
                            "timed ability requires positive duration and no channel interval"
                    );
                }
            }
            case CHANNEL -> {
                if (castDurationTicks < 1
                        || channelIntervalTicks.isEmpty()
                        || channelIntervalTicks.getAsLong() < 1
                        || channelIntervalTicks.getAsLong() > castDurationTicks) {
                    throw new IllegalArgumentException(
                            "channel requires positive duration and interval <= duration"
                    );
                }
            }
        }
    }

    /**
     * Compatibility constructor for existing P3 fixtures/tests.
     *
     * <p>The original P3 behavior was stationary casting, so the compatibility
     * default preserves that behavior explicitly as INTERRUPT.</p>
     */
    public AbilityDefinition(
            RpgId id,
            AbilityCastKind castKind,
            long castDurationTicks,
            OptionalLong channelIntervalTicks,
            long cooldownTicks,
            long globalCooldownTicks,
            List<AbilityCost> costs,
            Condition<AbilityContext> activationCondition,
            EffectSequence effects
    ) {
        this(
                id,
                castKind,
                castDurationTicks,
                channelIntervalTicks,
                cooldownTicks,
                globalCooldownTicks,
                costs,
                activationCondition,
                effects,
                AbilityMovementPolicy.INTERRUPT
        );
    }
}
