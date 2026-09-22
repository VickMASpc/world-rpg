package dev.worldrpg.content.combat;

import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.ReloadSafety;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
import dev.worldrpg.content.load.DefinitionDomainHandler;
import dev.worldrpg.content.load.DefinitionResolver;
import dev.worldrpg.content.load.DefinitionValidator;

public final class CombatContentDomains {
    public static final RegistryKey<AuraContentDefinition> AURAS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/auras"),
                    AuraContentDefinition.class
            );

    public static final RegistryKey<AbilityContentDefinition> ABILITIES =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/abilities"),
                    AbilityContentDefinition.class
            );

    private static final DefinitionDomain<AuraContentDefinition> AURA_DOMAIN =
            new DefinitionDomain<>(
                    AURAS,
                    new SchemaVersion(1),
                    ReloadSafety.SAFE
            );

    private static final DefinitionDomain<AbilityContentDefinition> ABILITY_DOMAIN =
            new DefinitionDomain<>(
                    ABILITIES,
                    new SchemaVersion(1),
                    ReloadSafety.SAFE
            );

    private CombatContentDomains() {
    }

    public static DefinitionDomainCatalog catalog() {
        DefinitionDomainHandler<AuraContentDefinition> auraHandler =
                new DefinitionDomainHandler<>(
                        AURA_DOMAIN,
                        CombatContentDecoders::decodeAura,
                        DefinitionResolver.none(),
                        DefinitionValidator.none()
                );

        DefinitionDomainHandler<AbilityContentDefinition> abilityHandler =
                new DefinitionDomainHandler<>(
                        ABILITY_DOMAIN,
                        CombatContentDecoders::decodeAbility,
                        AbilityContentDefinition::resolveReferences,
                        DefinitionValidator.none()
                );

        return DefinitionDomainCatalog.builder()
                .add(auraHandler)
                .add(abilityHandler)
                .addCrossRegistryValidator((candidate, report) -> {
                    try {
                        P3CombatContentCompiler.compile(candidate);
                    } catch (RuntimeException exception) {
                        report.error(
                                "combat.compile",
                                exception.getMessage() == null
                                        ? exception.getClass().getSimpleName()
                                        : exception.getMessage(),
                                SourceRef.of("<combat-content-compiler>")
                        );
                    }
                })
                .build();
    }
}
