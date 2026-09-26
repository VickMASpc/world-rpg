package dev.worldrpg.combat.condition;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.resource.ResourcePool;

import java.util.Objects;
import java.util.Optional;

public final class ResourceConditions {
    private static final RpgId MISSING_RESOURCE =
            RpgId.parse("world_rpg:condition/missing_resource_pool");
    private static final RpgId SOURCE_RESOURCE_TOO_LOW =
            RpgId.parse("world_rpg:condition/source_resource_too_low");
    private static final RpgId TARGET_RESOURCE_TOO_LOW =
            RpgId.parse("world_rpg:condition/target_resource_too_low");
    private static final RpgId SOURCE_RESOURCE_ABOVE_FRACTION =
            RpgId.parse(
                    "world_rpg:condition/source_resource_above_fraction"
            );
    private static final RpgId TARGET_RESOURCE_ABOVE_FRACTION =
            RpgId.parse(
                    "world_rpg:condition/target_resource_above_fraction"
            );

    private ResourceConditions() {
    }

    public static Condition<AbilityContext> sourceAtLeast(
            ResourceKey resource,
            double amount
    ) {
        validate(resource, amount);

        return context -> atLeast(
                context.source(),
                resource,
                amount,
                SOURCE_RESOURCE_TOO_LOW,
                "Source"
        );
    }

    public static Condition<AbilityContext> targetAtLeast(
            ResourceKey resource,
            double amount
    ) {
        validate(resource, amount);

        return context -> atLeast(
                context.target(),
                resource,
                amount,
                TARGET_RESOURCE_TOO_LOW,
                "Target"
        );
    }

    public static Condition<AbilityContext> sourceAtOrBelowFraction(
            ResourceKey resource,
            double fraction
    ) {
        validateFraction(resource, fraction);

        return context -> atOrBelowFraction(
                context.source(),
                resource,
                fraction,
                SOURCE_RESOURCE_ABOVE_FRACTION,
                "Source"
        );
    }

    public static Condition<AbilityContext> targetAtOrBelowFraction(
            ResourceKey resource,
            double fraction
    ) {
        validateFraction(resource, fraction);

        return context -> atOrBelowFraction(
                context.target(),
                resource,
                fraction,
                TARGET_RESOURCE_ABOVE_FRACTION,
                "Target"
        );
    }

    private static ConditionResult atOrBelowFraction(
            CombatActor actor,
            ResourceKey resource,
            double fraction,
            RpgId failureCode,
            String label
    ) {
        Optional<ResourcePool> pool = actor.resources().find(resource);

        if (pool.isEmpty()) {
            return ConditionResult.fail(
                    MISSING_RESOURCE,
                    label + " does not have resource " + resource
            );
        }

        double threshold =
                pool.get().maximum() * fraction;

        if (pool.get().current() > threshold) {
            return ConditionResult.fail(
                    failureCode,
                    label + " resource " + resource
                            + " is above "
                            + fraction
                            + " of maximum"
            );
        }

        return ConditionResult.pass();
    }

    private static ConditionResult atLeast(
            CombatActor actor,
            ResourceKey resource,
            double amount,
            RpgId failureCode,
            String label
    ) {
        Optional<ResourcePool> pool = actor.resources().find(resource);

        if (pool.isEmpty()) {
            return ConditionResult.fail(
                    MISSING_RESOURCE,
                    label + " does not have resource " + resource
            );
        }

        if (pool.get().current() < amount) {
            return ConditionResult.fail(
                    failureCode,
                    label + " requires at least " + amount + " " + resource
            );
        }

        return ConditionResult.pass();
    }

    private static void validateFraction(
            ResourceKey resource,
            double fraction
    ) {
        Objects.requireNonNull(resource, "resource");

        if (!Double.isFinite(fraction)
                || fraction < 0.0
                || fraction > 1.0) {
            throw new IllegalArgumentException(
                    "resource fraction must be finite and in [0, 1]"
            );
        }
    }

    private static void validate(
            ResourceKey resource,
            double amount
    ) {
        Objects.requireNonNull(resource, "resource");

        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException(
                    "resource condition amount must be finite and >= 0"
            );
        }
    }
}
