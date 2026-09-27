package dev.worldrpg.content.adventure;

import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.enemy.MobContentDefinition;

import java.util.Objects;
import java.util.regex.Pattern;

public sealed interface QuestObjectiveSpec permits
        QuestObjectiveSpec.VisitLocation,
        QuestObjectiveSpec.SpeakToNpc,
        QuestObjectiveSpec.DefeatMob {
    Pattern KEY_PATTERN = Pattern.compile("[a-z0-9_.-]+");

    String key();

    void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    );

    static String validateKey(String key) {
        Objects.requireNonNull(key, "key");
        String normalized = key.trim();
        if (!KEY_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "quest objective key must match [a-z0-9_.-]+: " + key
            );
        }
        return normalized;
    }

    record VisitLocation(
            String key,
            RequiredDefinitionRef<WorldLocationContentDefinition> location
    ) implements QuestObjectiveSpec {
        public VisitLocation {
            key = validateKey(key);
            Objects.requireNonNull(location, "location");
        }

        @Override
        public void resolveReferences(
                RegistrySnapshot snapshot,
                SourceRef source,
                ValidationReport report
        ) {
            ReferenceResolver.resolve(
                    location,
                    snapshot,
                    report,
                    source
            );
        }
    }

    record SpeakToNpc(
            String key,
            RequiredDefinitionRef<NpcContentDefinition> npc
    ) implements QuestObjectiveSpec {
        public SpeakToNpc {
            key = validateKey(key);
            Objects.requireNonNull(npc, "npc");
        }

        @Override
        public void resolveReferences(
                RegistrySnapshot snapshot,
                SourceRef source,
                ValidationReport report
        ) {
            ReferenceResolver.resolve(
                    npc,
                    snapshot,
                    report,
                    source
            );
        }
    }

    record DefeatMob(
            String key,
            RequiredDefinitionRef<MobContentDefinition> mob
    ) implements QuestObjectiveSpec {
        public DefeatMob {
            key = validateKey(key);
            Objects.requireNonNull(mob, "mob");
        }

        @Override
        public void resolveReferences(
                RegistrySnapshot snapshot,
                SourceRef source,
                ValidationReport report
        ) {
            ReferenceResolver.resolve(
                    mob,
                    snapshot,
                    report,
                    source
            );
        }

        public static RequiredDefinitionRef<MobContentDefinition>
        mobRef(dev.worldrpg.api.id.RpgId id) {
            return new RequiredDefinitionRef<>(
                    EnemyContentDomains.MOBS,
                    id
            );
        }
    }
}
