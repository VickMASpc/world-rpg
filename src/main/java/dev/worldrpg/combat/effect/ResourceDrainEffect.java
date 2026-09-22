package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.resource.ResourcePool;

import java.util.List;
import java.util.Objects;

public record ResourceDrainEffect(
        EffectRecipient recipient,
        ResourceKey resource,
        double amount
) implements CombatEffect {
    private static final RpgId MISSING_RESOURCE =
            RpgId.parse("world_rpg:condition/missing_resource_pool");

    public ResourceDrainEffect {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(resource, "resource");

        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("resource drain must be finite and >= 0");
        }
    }

    @Override
    public ConditionResult validate(EffectContext context) {
        CombatActor actor = recipient.resolve(context);

        return actor.resources().find(resource).isPresent()
                ? ConditionResult.pass()
                : ConditionResult.fail(
                        MISSING_RESOURCE,
                        "Actor does not have resource pool " + resource
                );
    }

    @Override
    public List<CombatEvent> apply(EffectContext context) {
        CombatActor actor = recipient.resolve(context);
        ResourcePool pool = actor.resources().require(resource);
        ResourceChange change = pool.drainUpTo(amount);

        return List.of(new ResourceChangedEvent(
                context.gameTick(),
                actor.id(),
                resource,
                change
        ));
    }
}
