package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.aura.AuraRemoval;
import dev.worldrpg.combat.aura.AuraRemovalReason;
import dev.worldrpg.combat.event.AuraRemovedEvent;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record RemoveAuraEffect(
        EffectRecipient recipient,
        RpgId auraId,
        AuraRemovalReason reason
) implements CombatEffect {
    public RemoveAuraEffect {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(auraId, "auraId");
        Objects.requireNonNull(reason, "reason");
    }

    @Override
    public List<CombatEvent> apply(EffectContext context) {
        CombatActor target = recipient.resolve(context);
        List<AuraRemoval> removals =
                target.auras().removeByDefinition(auraId, reason);

        List<CombatEvent> events = new ArrayList<>();
        for (AuraRemoval removal : removals) {
            events.add(new AuraRemovedEvent(
                    context.gameTick(),
                    removal.instance().source(),
                    target.id(),
                    auraId,
                    removal.instance().id(),
                    reason
            ));
        }

        return List.copyOf(events);
    }
}
