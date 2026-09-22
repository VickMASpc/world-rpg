package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import net.minecraft.entity.LivingEntity;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Raw Minecraft observations used by later target-condition adapters.
 *
 * <p>This record does not decide whether a target is legal. It only captures
 * facts so ability rules remain data/kernel policy rather than integration policy.</p>
 */
public record MinecraftTargetProbe(
        CombatActorId sourceActor,
        CombatActorId targetActor,
        boolean sameEntity,
        boolean sameWorld,
        boolean sourceAlive,
        boolean targetAlive,
        boolean lineOfSight,
        OptionalDouble squaredDistance
) {
    public MinecraftTargetProbe {
        Objects.requireNonNull(sourceActor, "sourceActor");
        Objects.requireNonNull(targetActor, "targetActor");
        Objects.requireNonNull(squaredDistance, "squaredDistance");
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

        return new MinecraftTargetProbe(
                sourceActor.id(),
                targetActor.id(),
                source == target,
                sameWorld,
                source.isAlive(),
                target.isAlive(),
                sameWorld && source.canSee(target),
                sameWorld
                        ? OptionalDouble.of(source.squaredDistanceTo(target))
                        : OptionalDouble.empty()
        );
    }

    public String summary() {
        String distance = squaredDistance.isPresent()
                ? Double.toString(Math.sqrt(squaredDistance.getAsDouble()))
                : "cross-world";

        return "sourceActor=" + sourceActor.value()
                + " targetActor=" + targetActor.value()
                + " sameEntity=" + sameEntity
                + " sameWorld=" + sameWorld
                + " sourceAlive=" + sourceAlive
                + " targetAlive=" + targetAlive
                + " lineOfSight=" + lineOfSight
                + " distance=" + distance;
    }
}
