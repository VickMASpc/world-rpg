package dev.worldrpg.content.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.effect.ApplyAuraEffect;
import dev.worldrpg.combat.effect.CombatEffect;
import dev.worldrpg.combat.effect.CombatMagnitudeEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatPowerScaling;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.resource.ResourceKey;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public sealed interface AbilityEffectSpec permits
        AbilityEffectSpec.ResourceDrain,
        AbilityEffectSpec.ApplyAura,
        AbilityEffectSpec.Magnitude {

    CombatEffect compile(
            RpgId abilityId,
            Map<RpgId, AuraDefinition> compiledAuras,
            CombatResolutionGateway resolutionGateway
    );

    record ResourceDrain(
            EffectRecipient recipient,
            ResourceKey resource,
            double amount
    ) implements AbilityEffectSpec {
        public ResourceDrain {
            Objects.requireNonNull(recipient, "recipient");
            Objects.requireNonNull(resource, "resource");
            if (!Double.isFinite(amount) || amount < 0.0) {
                throw new IllegalArgumentException(
                        "resource drain must be finite and >= 0"
                );
            }
        }

        @Override
        public CombatEffect compile(
                RpgId abilityId,
                Map<RpgId, AuraDefinition> compiledAuras,
                CombatResolutionGateway resolutionGateway
        ) {
            return new ResourceDrainEffect(
                    recipient,
                    resource,
                    amount
            );
        }
    }

    record ApplyAura(
            EffectRecipient recipient,
            RequiredDefinitionRef<AuraContentDefinition> aura
    ) implements AbilityEffectSpec {
        public ApplyAura {
            Objects.requireNonNull(recipient, "recipient");
            Objects.requireNonNull(aura, "aura");
        }

        @Override
        public CombatEffect compile(
                RpgId abilityId,
                Map<RpgId, AuraDefinition> compiledAuras,
                CombatResolutionGateway resolutionGateway
        ) {
            AuraDefinition definition = compiledAuras.get(
                    aura.id()
            );
            if (definition == null) {
                throw new NoSuchElementException(
                        "Referenced aura was not compiled: "
                                + aura.id()
                );
            }
            return new ApplyAuraEffect(
                    recipient,
                    definition
            );
        }
    }

    record Magnitude(
            EffectRecipient recipient,
            CombatMagnitudeKind kind,
            RpgId schoolId,
            RpgId resolutionProfileId,
            CombatPowerScaling powerScaling,
            double baseMagnitude
    ) implements AbilityEffectSpec {
        public Magnitude {
            Objects.requireNonNull(recipient, "recipient");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(schoolId, "schoolId");
            Objects.requireNonNull(
                    resolutionProfileId,
                    "resolutionProfileId"
            );
            Objects.requireNonNull(
                    powerScaling,
                    "powerScaling"
            );
            if (!Double.isFinite(baseMagnitude)
                    || baseMagnitude < 0.0) {
                throw new IllegalArgumentException(
                        "baseMagnitude must be finite and >= 0"
                );
            }
        }

        @Override
        public CombatEffect compile(
                RpgId abilityId,
                Map<RpgId, AuraDefinition> compiledAuras,
                CombatResolutionGateway resolutionGateway
        ) {
            return new CombatMagnitudeEffect(
                    recipient,
                    kind,
                    abilityId,
                    schoolId,
                    resolutionProfileId,
                    powerScaling,
                    baseMagnitude,
                    resolutionGateway
            );
        }
    }
}
