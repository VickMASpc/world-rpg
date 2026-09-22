package dev.worldrpg.content.combat;

import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.condition.Condition;
import dev.worldrpg.combat.condition.ResourceConditions;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.target.TargetConditions;

import java.util.Objects;

public sealed interface AbilityConditionSpec permits
        AbilityConditionSpec.SourceAlive,
        AbilityConditionSpec.TargetAlive,
        AbilityConditionSpec.SameWorld,
        AbilityConditionSpec.DisallowSelf,
        AbilityConditionSpec.RequireSelf,
        AbilityConditionSpec.LineOfSight,
        AbilityConditionSpec.MaxRange,
        AbilityConditionSpec.FacingArc,
        AbilityConditionSpec.TargetResourceAtLeast {

    Condition<AbilityContext> compile();

    record SourceAlive() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireSourceAlive();
        }
    }

    record TargetAlive() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireTargetAlive();
        }
    }

    record SameWorld() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireSameWorld();
        }
    }

    record DisallowSelf() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.disallowSelf();
        }
    }

    record RequireSelf() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireSelf();
        }
    }

    record LineOfSight() implements AbilityConditionSpec {
        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireLineOfSight();
        }
    }

    record MaxRange(double blocks) implements AbilityConditionSpec {
        public MaxRange {
            if (!Double.isFinite(blocks) || blocks < 0.0) {
                throw new IllegalArgumentException(
                        "max range must be finite and >= 0"
                );
            }
        }

        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.maxRange(blocks);
        }
    }

    record FacingArc(double degrees) implements AbilityConditionSpec {
        public FacingArc {
            if (!Double.isFinite(degrees)
                    || degrees < 0.0
                    || degrees > 360.0) {
                throw new IllegalArgumentException(
                        "facing arc must be finite and between 0 and 360"
                );
            }
        }

        @Override public Condition<AbilityContext> compile() {
            return TargetConditions.requireFacingArcDegrees(degrees);
        }
    }

    record TargetResourceAtLeast(
            ResourceKey resource,
            double amount
    ) implements AbilityConditionSpec {
        public TargetResourceAtLeast {
            Objects.requireNonNull(resource, "resource");
            if (!Double.isFinite(amount) || amount < 0.0) {
                throw new IllegalArgumentException(
                        "target resource threshold must be finite and >= 0"
                );
            }
        }

        @Override public Condition<AbilityContext> compile() {
            return ResourceConditions.targetAtLeast(resource, amount);
        }
    }
}
