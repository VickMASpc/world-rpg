package dev.worldrpg.content.enemy;

import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.ReloadSafety;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
import dev.worldrpg.content.load.DefinitionDomainHandler;

public final class EnemyContentDomains {
    public static final RegistryKey<LootTableContentDefinition> LOOT_TABLES =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/loot_tables"),
                    LootTableContentDefinition.class
            );

    public static final RegistryKey<MobContentDefinition> MOBS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/mobs"),
                    MobContentDefinition.class
            );

    private EnemyContentDomains() {
    }

    public static void addTo(
            DefinitionDomainCatalog.Builder builder
    ) {
        builder.add(new DefinitionDomainHandler<>(
                domain(LOOT_TABLES),
                EnemyContentDecoders::decodeLootTable,
                LootTableContentDefinition::resolveReferences,
                (definition, snapshot, source, report) -> {
                }
        ));
        builder.add(new DefinitionDomainHandler<>(
                domain(MOBS),
                EnemyContentDecoders::decodeMob,
                MobContentDefinition::resolveReferences,
                (definition, snapshot, source, report) -> {
                }
        ));
    }

    private static <T extends dev.worldrpg.api.data.RpgDefinition>
    DefinitionDomain<T> domain(RegistryKey<T> key) {
        return new DefinitionDomain<>(
                key,
                new SchemaVersion(1),
                ReloadSafety.SAFE
        );
    }
}
