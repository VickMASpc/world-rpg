package dev.worldrpg.combat.target;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.condition.Condition;
import dev.worldrpg.combat.condition.ConditionResult;

public final class TargetConditions {
    private static final RpgId OBSERVATION_UNAVAILABLE =
            RpgId.parse("world_rpg:condition/target_observation_unavailable");
    private static final RpgId SOURCE_DEAD =
            RpgId.parse("world_rpg:condition/source_dead");
    private static final RpgId TARGET_DEAD =
            RpgId.parse("world_rpg:condition/target_dead");
    private static final RpgId WRONG_WORLD =
            RpgId.parse("world_rpg:condition/target_wrong_world");
    private static final RpgId SELF_TARGET_DISALLOWED =
            RpgId.parse("world_rpg:condition/self_target_disallowed");
    private static final RpgId SELF_TARGET_REQUIRED =
            RpgId.parse("world_rpg:condition/self_target_required");
    private static final RpgId LINE_OF_SIGHT =
            RpgId.parse("world_rpg:condition/line_of_sight_required");
    private static final RpgId OUT_OF_RANGE =
            RpgId.parse("world_rpg:condition/target_out_of_range");
    private static final RpgId FACING_UNAVAILABLE =
            RpgId.parse("world_rpg:condition/target_facing_unavailable");
    private static final RpgId OUTSIDE_FACING_ARC =
            RpgId.parse("world_rpg:condition/target_outside_facing_arc");

    private TargetConditions() {
    }

    public static Condition<AbilityContext> requireSourceAlive() {
        return context -> withObservation(
                context,
                observation -> observation.sourceAlive()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(SOURCE_DEAD, "Source must be alive")
        );
    }

    public static Condition<AbilityContext> requireTargetAlive() {
        return context -> withObservation(
                context,
                observation -> observation.targetAlive()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(TARGET_DEAD, "Target must be alive")
        );
    }

    public static Condition<AbilityContext> requireSameWorld() {
        return context -> withObservation(
                context,
                observation -> observation.sameWorld()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(
                                WRONG_WORLD,
                                "Source and target must be in the same world"
                        )
        );
    }

    public static Condition<AbilityContext> disallowSelf() {
        return context -> withObservation(
                context,
                observation -> !observation.sameActor()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(
                                SELF_TARGET_DISALLOWED,
                                "Ability cannot target the source actor"
                        )
        );
    }

    public static Condition<AbilityContext> requireSelf() {
        return context -> withObservation(
                context,
                observation -> observation.sameActor()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(
                                SELF_TARGET_REQUIRED,
                                "Ability requires the source actor as target"
                        )
        );
    }

    public static Condition<AbilityContext> requireLineOfSight() {
        return context -> withObservation(
                context,
                observation -> observation.lineOfSight()
                        ? ConditionResult.pass()
                        : ConditionResult.fail(
                                LINE_OF_SIGHT,
                                "Target requires line of sight"
                        )
        );
    }

    public static Condition<AbilityContext> maxRange(double blocks) {
        if (!Double.isFinite(blocks) || blocks < 0.0) {
            throw new IllegalArgumentException(
                    "target range must be finite and >= 0"
            );
        }

        double maxSquared = blocks * blocks;

        return context -> withObservation(
                context,
                observation -> {
                    if (observation.squaredDistance().isEmpty()) {
                        return ConditionResult.fail(
                                OBSERVATION_UNAVAILABLE,
                                "Target distance is unavailable"
                        );
                    }

                    return observation.squaredDistance().getAsDouble() <= maxSquared
                            ? ConditionResult.pass()
                            : ConditionResult.fail(
                                    OUT_OF_RANGE,
                                    "Target is farther than " + blocks + " blocks"
                            );
                }
        );
    }

    public static Condition<AbilityContext> requireFacingArcDegrees(
            double fullArcDegrees
    ) {
        if (!Double.isFinite(fullArcDegrees)
                || fullArcDegrees < 0.0
                || fullArcDegrees > 360.0) {
            throw new IllegalArgumentException(
                    "facing arc must be finite and between 0 and 360 degrees"
            );
        }

        double threshold = Math.cos(
                Math.toRadians(fullArcDegrees / 2.0)
        );

        return context -> withObservation(
                context,
                observation -> {
                    if (observation.sourceFacingDot().isEmpty()) {
                        return ConditionResult.fail(
                                FACING_UNAVAILABLE,
                                "Source facing relative to target is unavailable"
                        );
                    }

                    return observation.sourceFacingDot().getAsDouble()
                            + 1.0e-12 >= threshold
                            ? ConditionResult.pass()
                            : ConditionResult.fail(
                                    OUTSIDE_FACING_ARC,
                                    "Target is outside the required "
                                            + fullArcDegrees
                                            + " degree facing arc"
                            );
                }
        );
    }

    private static ConditionResult withObservation(
            AbilityContext context,
            java.util.function.Function<TargetObservation, ConditionResult> rule
    ) {
        TargetObservation observation = context.targetObservation();

        if (!observation.available()) {
            return ConditionResult.fail(
                    OBSERVATION_UNAVAILABLE,
                    "Target observation is unavailable"
            );
        }

        return rule.apply(observation);
    }
}
