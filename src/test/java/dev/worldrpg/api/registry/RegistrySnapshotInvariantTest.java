package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrySnapshotInvariantTest {
    private static final RpgId SHARED_REGISTRY_ID =
            RpgId.parse("world_rpg:registry/shared");

    private static final RegistryKey<AlphaDefinition> ALPHA =
            new RegistryKey<>(SHARED_REGISTRY_ID, AlphaDefinition.class);

    private static final RegistryKey<BetaDefinition> BETA =
            new RegistryKey<>(SHARED_REGISTRY_ID, BetaDefinition.class);

    @Test
    void registryDomainIdCannotBeReusedWithDifferentJavaType() {
        ValidationReport report = new ValidationReport();

        DefinitionRegistryBuilder<AlphaDefinition> alphaBuilder =
                new DefinitionRegistryBuilder<>(ALPHA);
        alphaBuilder.add(
                new AlphaDefinition(RpgId.parse("world_rpg:test/alpha")),
                SourceRef.of("alpha.json"),
                report
        );

        DefinitionRegistryBuilder<BetaDefinition> betaBuilder =
                new DefinitionRegistryBuilder<>(BETA);
        betaBuilder.add(
                new BetaDefinition(RpgId.parse("world_rpg:test/beta")),
                SourceRef.of("beta.json"),
                report
        );

        RegistrySnapshot.Builder snapshotBuilder = RegistrySnapshot.builder();

        assertTrue(snapshotBuilder.add(alphaBuilder.build(), report));
        assertFalse(snapshotBuilder.add(betaBuilder.build(), report));
        assertTrue(report.hasErrors());
        assertEquals(1, snapshotBuilder.build().registryCount());
    }

    @Test
    void immutableRegistryPreservesDeterministicInsertionOrder() {
        ValidationReport report = new ValidationReport();
        RegistryKey<AlphaDefinition> key = new RegistryKey<>(
                RpgId.parse("world_rpg:registry/order_test"),
                AlphaDefinition.class
        );

        DefinitionRegistryBuilder<AlphaDefinition> builder =
                new DefinitionRegistryBuilder<>(key);

        RpgId first = RpgId.parse("world_rpg:test/first");
        RpgId second = RpgId.parse("world_rpg:test/second");

        builder.add(new AlphaDefinition(first), SourceRef.of("first.json"), report);
        builder.add(new AlphaDefinition(second), SourceRef.of("second.json"), report);

        DefinitionRegistry<AlphaDefinition> registry = builder.build();

        assertEquals(List.of(first, second), new ArrayList<>(registry.asMap().keySet()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> registry.asMap().clear()
        );
    }

    private record AlphaDefinition(RpgId id) implements RpgDefinition {
    }

    private record BetaDefinition(RpgId id) implements RpgDefinition {
    }
}
