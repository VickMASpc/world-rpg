package dev.worldrpg.content.enemy;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.ReferenceResolver;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.adventure.WorldLocationContentDefinition;

import java.util.Objects;

public record SpawnGroupContentDefinition(
        RpgId id,
        RequiredDefinitionRef<WorldLocationContentDefinition> location,
        RequiredDefinitionRef<MobContentDefinition> mob,
        int targetPopulation,
        long respawnTicks,
        double spawnRadius,
        double leashRadius
) implements RpgDefinition {
    public SpawnGroupContentDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(mob, "mob");
        if (targetPopulation < 1 || targetPopulation > 32) {
            throw new IllegalArgumentException(
                    "targetPopulation must be between 1 and 32"
            );
        }
        if (respawnTicks < 20L) {
            throw new IllegalArgumentException(
                    "respawnTicks must be >= 20"
            );
        }
        if (!Double.isFinite(spawnRadius)
                || spawnRadius <= 0.0) {
            throw new IllegalArgumentException(
                    "spawnRadius must be finite and > 0"
            );
        }
        if (!Double.isFinite(leashRadius)
                || leashRadius < spawnRadius) {
            throw new IllegalArgumentException(
                    "leashRadius must be finite and >= spawnRadius"
            );
        }
    }

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
        ReferenceResolver.resolve(
                mob,
                snapshot,
                report,
                source
        );
    }
}
