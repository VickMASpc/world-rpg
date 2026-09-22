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
        boolean sameWorld = source.getWorld() == target.getWorld();

        TargetObservation observation = TargetObservation.observed(
                source == target,
                sameWorld,
                source.isAlive(),
                target.isAlive(),
                sameWorld && source.canSee(target),
                sameWorld
                        ? java.util.OptionalDouble.of(
                                source.squaredDistanceTo(target)
                        )
                        : java.util.OptionalDouble.empty()
        );

        return new MinecraftTargetProbe(
                sourceActor.id(),
                targetActor.id(),
                observation
        );
    }

    public String summary() {
        String distance = observation.squaredDistance().isPresent()
                ? Double.toString(
                        Math.sqrt(observation.squaredDistance().getAsDouble())
                )
                : "cross-world";

        return "sourceActor=" + sourceActor.value()
                + " targetActor=" + targetActor.value()
                + " sameEntity=" + observation.sameActor()
                + " sameWorld=" + observation.sameWorld()
                + " sourceAlive=" + observation.sourceAlive()
                + " targetAlive=" + observation.targetAlive()
                + " lineOfSight=" + observation.lineOfSight()
                + " distance=" + distance;
    }
}
