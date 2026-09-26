package dev.worldrpg.content.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class P3CombatContentCompiler {
    private P3CombatContentCompiler() {
    }

    public static P3CombatContentSnapshot compile(
            RegistrySnapshot snapshot
    ) {
        return compile(
                snapshot,
                request -> java.util.List.of()
        );
    }

    public static P3CombatContentSnapshot compile(
            RegistrySnapshot snapshot,
            CombatResolutionGateway resolutionGateway
    ) {
        Objects.requireNonNull(snapshot, "snapshot");

        Map<RpgId, AuraDefinition> compiledAuras =
                new LinkedHashMap<>();

        for (AuraContentDefinition aura :
                snapshot.require(CombatContentDomains.AURAS).values()) {
            AuraDefinition compiled = aura.compile();
            compiledAuras.put(compiled.id(), compiled);
        }

        Map<RpgId, AbilityDefinition> compiledAbilities =
                new LinkedHashMap<>();

        for (AbilityContentDefinition ability :
                snapshot.require(CombatContentDomains.ABILITIES).values()) {
            AbilityDefinition compiled =
                    ability.compile(
                            compiledAuras,
                            resolutionGateway
                    );
            compiledAbilities.put(compiled.id(), compiled);
        }

        return new P3CombatContentSnapshot(
                compiledAuras,
                compiledAbilities
        );
    }
}
