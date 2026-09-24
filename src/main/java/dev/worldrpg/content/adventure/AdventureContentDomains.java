package dev.worldrpg.content.adventure;

import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.ReloadSafety;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
import dev.worldrpg.content.load.DefinitionDomainHandler;

public final class AdventureContentDomains {
    public static final RegistryKey<WorldLocationContentDefinition> WORLD_LOCATIONS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/world_locations"),
                    WorldLocationContentDefinition.class
            );

    public static final RegistryKey<NpcContentDefinition> NPCS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/npcs"),
                    NpcContentDefinition.class
            );

    public static final RegistryKey<ItemContentDefinition> ITEMS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/items"),
                    ItemContentDefinition.class
            );

    public static final RegistryKey<QuestContentDefinition> QUESTS =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/quests"),
                    QuestContentDefinition.class
            );

    private AdventureContentDomains() {
    }

    public static void addTo(DefinitionDomainCatalog.Builder builder) {
        builder.add(DefinitionDomainHandler.of(
                domain(WORLD_LOCATIONS),
                AdventureContentDecoders::decodeLocation
        ));
        builder.add(new DefinitionDomainHandler<>(
                domain(NPCS),
                AdventureContentDecoders::decodeNpc,
                NpcContentDefinition::resolveReferences,
                (definition, snapshot, source, report) -> {
                }
        ));
        builder.add(DefinitionDomainHandler.of(
                domain(ITEMS),
                AdventureContentDecoders::decodeItem
        ));
        builder.add(new DefinitionDomainHandler<>(
                domain(QUESTS),
                AdventureContentDecoders::decodeQuest,
                QuestContentDefinition::resolveReferences,
                (definition, snapshot, source, report) -> {
                }
        ));
    }

    public static DefinitionDomainCatalog catalog() {
        DefinitionDomainCatalog.Builder builder = DefinitionDomainCatalog.builder();
        addTo(builder);
        return builder.build();
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
