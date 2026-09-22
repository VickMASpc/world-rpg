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
        OptionalDouble squaredDistance
) {
    public TargetObservation {
        Objects.requireNonNull(squaredDistance, "squaredDistance");

        if (!available && squaredDistance.isPresent()) {
            throw new IllegalArgumentException(
                    "unavailable target observation cannot carry distance"
            );
        }

        if (squaredDistance.isPresent()) {
            double value = squaredDistance.getAsDouble();
            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalArgumentException(
                        "squaredDistance must be finite and >= 0"
                );
            }
        }
    }

    public static TargetObservation unavailable() {
        return new TargetObservation(
                false,
                false,
                false,
                false,
                false,
                false,
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
        return new TargetObservation(
                true,
                sameActor,
                sameWorld,
                sourceAlive,
                targetAlive,
                lineOfSight,
                squaredDistance
        );
    }
}
