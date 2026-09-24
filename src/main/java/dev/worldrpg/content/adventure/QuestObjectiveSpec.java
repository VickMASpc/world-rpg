package dev.worldrpg.content.adventure;

import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Objects;

public sealed interface QuestObjectiveSpec permits
        QuestObjectiveSpec.VisitLocation,
        QuestObjectiveSpec.SpeakToNpc {

    void resolveReferences(
            RegistrySnapshot snapshot,
            SourceRef source,
            ValidationReport report
    );

    record VisitLocation(
            RequiredDefinitionRef<WorldLocationContentDefinition> location
    ) implements QuestObjectiveSpec {
        public VisitLocation {
            Objects.requireNonNull(location, "location");
        }

        @Override
        public void resolveReferences(
                RegistrySnapshot snapshot,
                SourceRef source,
                ValidationReport report
        ) {
            ReferenceResolver.resolve(location, snapshot, report, source);
        }
    }

    record SpeakToNpc(
            RequiredDefinitionRef<NpcContentDefinition> npc
    ) implements QuestObjectiveSpec {
        public SpeakToNpc {
            Objects.requireNonNull(npc, "npc");
        }

        @Override
        public void resolveReferences(
                RegistrySnapshot snapshot,
                SourceRef source,
                ValidationReport report
        ) {
            ReferenceResolver.resolve(npc, snapshot, report, source);
        }
    }
}
