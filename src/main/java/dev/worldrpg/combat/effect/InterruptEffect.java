package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.cast.CastInterruptionRequest;
import dev.worldrpg.combat.cast.CombatCastControlGateway;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;

/**
 * Generic effect that interrupts the selected recipient's active cast through
 * an injected cast-control gateway.
 */
public final class InterruptEffect implements CombatEffect {
    private final EffectRecipient recipient;
    private final CombatCastControlGateway gateway;

    public InterruptEffect(
            EffectRecipient recipient,
            CombatCastControlGateway gateway
    ) {
        this.recipient =
                Objects.requireNonNull(recipient, "recipient");
        this.gateway =
                Objects.requireNonNull(gateway, "gateway");
    }

    @Override
    public ConditionResult validate(EffectContext context) {
        return gateway.validateInterrupt(
                request(context)
        );
    }

    @Override
    public List<CombatEvent> apply(EffectContext context) {
        return gateway.interrupt(
                request(context)
        );
    }

    private CastInterruptionRequest request(
            EffectContext context
    ) {
        Objects.requireNonNull(context, "context");

        return new CastInterruptionRequest(
                context.gameTick(),
                context.source(),
                recipient.resolve(context),
                CastInterruptionReason.INTERRUPT
        );
    }
}
