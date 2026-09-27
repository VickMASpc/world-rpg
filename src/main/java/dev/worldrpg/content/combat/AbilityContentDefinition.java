package dev.worldrpg.content.combat;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityMovementPolicy;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.condition.Condition;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.CombatEffect;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

public record AbilityContentDefinition(
        RpgId id,
        AbilityCastKind castKind,
        long castDurationTicks,
        OptionalLong channelIntervalTicks,
        long cooldownTicks,
        long globalCooldownTicks,
        AbilityMovementPolicy movementPolicy,
        List<AbilityCost> costs,
        List<AbilityConditionSpec> conditions,
        List<AbilityEffectSpec> effects
) implements RpgDefinition {
    public AbilityContentDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(castKind, "castKind");
        Objects.requireNonNull(channelIntervalTicks, "channelIntervalTicks");
        Objects.requireNonNull(movementPolicy, "movementPolicy");
        costs = List.copyOf(Objects.requireNonNull(costs, "costs"));
        conditions = List.copyOf(
                Objects.requireNonNull(conditions, "conditions")
        );
        effects = List.copyOf(
                Objects.requireNonNull(effects, "effects")
        );

        new AbilityDefinition(
                id,
                castKind,
                castDurationTicks,
                channelIntervalTicks,
                cooldownTicks,
                globalCooldownTicks,
                costs,
                Conditions.all(List.of()),
                new EffectSequence(List.of()),
                movementPolicy
        );
    }

    public void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    ) {
        for (AbilityEffectSpec effect : effects) {
            if (effect instanceof AbilityEffectSpec.ApplyAura applyAura) {
                ReferenceResolver.resolve(
                        applyAura.aura(),
                        snapshot,
                        report,
                        source
                );
            }
        }
    }

    public AbilityDefinition compile(
            Map<RpgId, AuraDefinition> compiledAuras,
            CombatResolutionGateway resolutionGateway
    ) {
        List<Condition<AbilityContext>> compiledConditions =
                conditions.stream()
                        .map(AbilityConditionSpec::compile)
                        .toList();

        List<CombatEffect> compiledEffects =
                effects.stream()
                        .map(effect -> effect.compile(
                                id,
                                compiledAuras,
                                resolutionGateway
                        ))
                        .toList();

        return new AbilityDefinition(
                id,
                castKind,
                castDurationTicks,
                channelIntervalTicks,
                cooldownTicks,
                globalCooldownTicks,
                costs,
                Conditions.all(compiledConditions),
                new EffectSequence(compiledEffects),
                movementPolicy
        );
    }

    public AbilityDefinition compile(
            Map<RpgId, AuraDefinition> compiledAuras
    ) {
        return compile(
                compiledAuras,
                request -> java.util.List.of()
        );
    }
}
