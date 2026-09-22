package dev.worldrpg.combat.target;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Integration-independent observation of source/target facts at one moment.
 *
 * <p>Rules consume this snapshot; integrations produce it. The snapshot carries
 * no policy about whether any fact is legal for a particular ability.</p>
 */
public record TargetObservation(
        boolean available,
        boolean sameActor,
        boolean sameWorld,
        boolean sourceAlive,
        boolean targetAlive,
        boolean lineOfSight,
        OptionalDouble squaredDistance,
        OptionalDouble sourceFacingDot
) {
    public TargetObservation {
        Objects.requireNonNull(squaredDistance, "squaredDistance");
        Objects.requireNonNull(sourceFacingDot, "sourceFacingDot");

        if (!available
                && (squaredDistance.isPresent() || sourceFacingDot.isPresent())) {
            throw new IllegalArgumentException(
                    "unavailable target observation cannot carry spatial facts"
            );
        }

        validateFiniteRange(
                squaredDistance,
                0.0,
                Double.POSITIVE_INFINITY,
                "squaredDistance"
        );
        validateFiniteRange(
                sourceFacingDot,
                -1.0,
                1.0,
                "sourceFacingDot"
        );
    }

    public static TargetObservation unavailable() {
        return new TargetObservation(
                false,
                false,
                false,
                false,
                false,
                false,
                OptionalDouble.empty(),
                OptionalDouble.empty()
        );
    }

    public static TargetObservation observed(
            boolean sameActor,
            boolean sameWorld,
            boolean sourceAlive,
            boolean targetAlive,
            boolean lineOfSight,
            OptionalDouble squaredDistance
    ) {
        return observed(
                sameActor,
                sameWorld,
                sourceAlive,
                targetAlive,
                lineOfSight,
                squaredDistance,
                OptionalDouble.empty()
        );
    }

    public static TargetObservation observed(
            boolean sameActor,
            boolean sameWorld,
            boolean sourceAlive,
            boolean targetAlive,
            boolean lineOfSight,
            OptionalDouble squaredDistance,
            OptionalDouble sourceFacingDot
    ) {
        return new TargetObservation(
                true,
                sameActor,
                sameWorld,
                sourceAlive,
                targetAlive,
                lineOfSight,
                squaredDistance,
                sourceFacingDot
        );
    }

    private static void validateFiniteRange(
            OptionalDouble value,
            double minimum,
            double maximum,
            String label
    ) {
        if (value.isEmpty()) {
            return;
        }

        double raw = value.getAsDouble();
        if (!Double.isFinite(raw)
                || raw < minimum
                || raw > maximum) {
            throw new IllegalArgumentException(
                    label + " must be finite and between "
                            + minimum + " and " + maximum
            );
        }
    }
}
