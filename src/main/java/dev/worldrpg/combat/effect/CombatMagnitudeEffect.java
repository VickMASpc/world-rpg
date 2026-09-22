package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;

import java.util.List;
import java.util.Objects;

public final class CombatMagnitudeEffect implements CombatEffect {
    private final EffectRecipient recipient;
    private final CombatMagnitudeKind kind;
    private final RpgId causeId;
    private final RpgId schoolId;
    private final RpgId resolutionProfileId;
    private final double authoredBaseMagnitude;
    private final CombatResolutionGateway gateway;

    public CombatMagnitudeEffect(
            EffectRecipient recipient,
            CombatMagnitudeKind kind,
            RpgId causeId,
            RpgId schoolId,
            RpgId resolutionProfileId,
            double authoredBaseMagnitude,
            CombatResolutionGateway gateway
    ) {
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.causeId = Objects.requireNonNull(causeId, "causeId");
        this.schoolId = Objects.requireNonNull(schoolId, "schoolId");
        this.resolutionProfileId = Objects.requireNonNull(
                resolutionProfileId,
                "resolutionProfileId"
        );
        this.gateway = Objects.requireNonNull(gateway, "gateway");

        if (!Double.isFinite(authoredBaseMagnitude)
                || authoredBaseMagnitude < 0.0) {
            throw new IllegalArgumentException(
                    "authoredBaseMagnitude must be finite and >= 0"
            );
        }

        this.authoredBaseMagnitude = authoredBaseMagnitude;
    }

    @Override
    public ConditionResult validate(EffectContext context) {
        return gateway.validate(request(context));
    }

    @Override
    public List<CombatEvent> apply(EffectContext context) {
        return gateway.resolve(request(context));
    }

    private CombatMagnitudeRequest request(EffectContext context) {
        Objects.requireNonNull(context, "context");

        return new CombatMagnitudeRequest(
                context.gameTick(),
                kind,
                context.source(),
                recipient.resolve(context),
                causeId,
                schoolId,
                resolutionProfileId,
                authoredBaseMagnitude
        );
    }
}
