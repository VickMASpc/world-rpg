package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.target.TargetObservation;
import net.minecraft.entity.LivingEntity;

import java.util.Objects;

/**
 * Raw Minecraft observations used by later target-condition adapters.
 */
public record MinecraftTargetProbe(
        CombatActorId sourceActor,
        CombatActorId targetActor,
        TargetObservation observation
) {
    public MinecraftTargetProbe {
        Objects.requireNonNull(sourceActor, "sourceActor");
        Objects.requireNonNull(targetActor, "targetActor");
        Objects.requireNonNull(observation, "observation");
    }

    public static MinecraftTargetProbe capture(
            LivingEntity source,
            LivingEntity target,
            MinecraftCombatActorBindings bindings
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(bindings, "bindings");

        CombatActor sourceActor = bindings.bind(source);
        CombatActor targetActor = bindings.bind(target);

        return new MinecraftTargetProbe(
                sourceActor.id(),
                targetActor.id(),
                MinecraftTargetObservationProvider.capture(
                        source,
                        target
                )
        );
    }

    public String summary() {
        String distance = observation.squaredDistance().isPresent()
                ? Double.toString(
                        Math.sqrt(observation.squaredDistance().getAsDouble())
                )
                : "cross-world";

        String facing = observation.sourceFacingDot().isPresent()
                ? Double.toString(
                        observation.sourceFacingDot().getAsDouble()
                )
                : "unavailable";

        return "sourceActor=" + sourceActor.value()
                + " targetActor=" + targetActor.value()
                + " sameEntity=" + observation.sameActor()
                + " sameWorld=" + observation.sameWorld()
                + " sourceAlive=" + observation.sourceAlive()
                + " targetAlive=" + observation.targetAlive()
                + " lineOfSight=" + observation.lineOfSight()
                + " distance=" + distance
                + " facingDot=" + facing;
    }
}
