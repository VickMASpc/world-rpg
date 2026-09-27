package dev.worldrpg.content.adventure;

import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.enemy.MobContentDefinition;

import java.util.Objects;

public final class QuestObjectiveText {
    private QuestObjectiveText() {
    }

    public static String describe(
            QuestObjectiveSpec objective,
            RegistrySnapshot snapshot
    ) {
        Objects.requireNonNull(objective, "objective");
        Objects.requireNonNull(snapshot, "snapshot");

        if (objective instanceof QuestObjectiveSpec.VisitLocation visit) {
            String name = snapshot
                    .require(AdventureContentDomains.WORLD_LOCATIONS)
                    .find(visit.location().id())
                    .map(WorldLocationContentDefinition::displayName)
                    .orElse(visit.location().id().toString());
            return "Visit " + name;
        }

        if (objective instanceof QuestObjectiveSpec.SpeakToNpc speak) {
            String name = snapshot
                    .require(AdventureContentDomains.NPCS)
                    .find(speak.npc().id())
                    .map(NpcContentDefinition::displayName)
                    .orElse(speak.npc().id().toString());
            return "Speak to " + name;
        }

        if (objective instanceof QuestObjectiveSpec.DefeatMob defeat) {
            String name = snapshot
                    .require(EnemyContentDomains.MOBS)
                    .find(defeat.mob().id())
                    .map(MobContentDefinition::displayName)
                    .orElse(defeat.mob().id().toString());
            return "Defeat " + name;
        }

        throw new IllegalStateException(
                "Unsupported quest objective type: "
                        + objective.getClass().getName()
        );
    }
}
