package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.aura.AuraApplicationResult;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.event.AuraAppliedEvent;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;

public record ApplyAuraEffect(
        EffectRecipient recipient,
        AuraDefinition aura
) implements CombatEffect {
    public ApplyAuraEffect {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(aura, "aura");
    }

    @Override
    public List<CombatEvent> apply(EffectContext context) {
        CombatActor target = recipient.resolve(context);
        AuraApplicationResult result = target.auras().apply(
                aura,
                context.source().id(),
                context.gameTick()
        );

        return List.of(new AuraAppliedEvent(
                context.gameTick(),
                context.source().id(),
                target.id(),
                aura.id(),
                result.instance().id(),
                result.created(),
                result.previousStacks(),
                result.currentStacks()
        ));
    }
}
