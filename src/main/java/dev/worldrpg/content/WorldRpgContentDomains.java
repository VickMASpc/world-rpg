package dev.worldrpg.content;

import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.combat.CombatContentDomains;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.load.DefinitionDomainCatalog;

public final class WorldRpgContentDomains {
    private WorldRpgContentDomains() {
    }

    public static DefinitionDomainCatalog catalog() {
        DefinitionDomainCatalog.Builder builder =
                DefinitionDomainCatalog.builder();

        CombatContentDomains.addTo(builder);
        AdventureContentDomains.addTo(builder);
        EnemyContentDomains.addTo(builder);

        return builder.build();
    }
}
