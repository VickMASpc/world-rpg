package dev.worldrpg.content.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.aura.AuraDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public record P3CombatContentSnapshot(
        Map<RpgId, AuraDefinition> auras,
        Map<RpgId, AbilityDefinition> abilities
) {
    private static final P3CombatContentSnapshot EMPTY =
            new P3CombatContentSnapshot(Map.of(), Map.of());

    public P3CombatContentSnapshot {
        auras = java.util.Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Objects.requireNonNull(auras, "auras")
                )
        );
        abilities = java.util.Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Objects.requireNonNull(abilities, "abilities")
                )
        );
    }

    public static P3CombatContentSnapshot empty() {
        return EMPTY;
    }

    public AbilityDefinition requireAbility(RpgId id) {
        AbilityDefinition ability = abilities.get(
                Objects.requireNonNull(id, "id")
        );
        if (ability == null) {
            throw new NoSuchElementException(
                    "Missing compiled ability: " + id
            );
        }
        return ability;
    }

    public AuraDefinition requireAura(RpgId id) {
        AuraDefinition aura = auras.get(
                Objects.requireNonNull(id, "id")
        );
        if (aura == null) {
            throw new NoSuchElementException(
                    "Missing compiled aura: " + id
            );
        }
        return aura;
    }
}
