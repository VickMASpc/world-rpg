package dev.worldrpg.sim;

import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.CastController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal deterministic rotation policy: choose the first ability in authored
 * priority order that passes the production CastController preflight.
 */
public final class PriorityAbilityPolicy {
    private final List<AbilityDefinition> priority;

    public PriorityAbilityPolicy(
            List<AbilityDefinition> priority
    ) {
        this.priority = List.copyOf(
                Objects.requireNonNull(
                        priority,
                        "priority"
                )
        );

        if (this.priority.isEmpty()) {
            throw new IllegalArgumentException(
                    "priority must not be empty"
            );
        }
    }

    public List<AbilityDefinition> priority() {
        return priority;
    }

    public Optional<AbilityDefinition> choose(
            CastController controller,
            CombatActor target,
            long gameTick
    ) {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(target, "target");

        for (AbilityDefinition ability : priority) {
            if (controller.validateActivation(
                    ability,
                    target,
                    gameTick
            ).passed()) {
                return Optional.of(ability);
            }
        }

        return Optional.empty();
    }
}
