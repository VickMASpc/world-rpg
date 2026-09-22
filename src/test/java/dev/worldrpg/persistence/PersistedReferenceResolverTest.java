package dev.worldrpg.persistence;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.DefinitionRegistryBuilder;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedReferenceResolverTest {
    private static final RegistryKey<Fixture> FIXTURES =
            new RegistryKey<>(
                    RpgId.parse("world_rpg:registry/persisted_fixture"),
                    Fixture.class
            );

    @Test
    void resolvesStablePointerAgainstPublishedShape() {
        Fixture definition =
                new Fixture(RpgId.parse("world_rpg:test/persisted"));

        DefinitionRegistryBuilder<Fixture> builder =
                new DefinitionRegistryBuilder<>(FIXTURES);
        ValidationReport report = new ValidationReport();
        builder.add(
                definition,
                SourceRef.of("fixture"),
                report
        );

        RegistrySnapshot.Builder snapshot =
                RegistrySnapshot.builder();
        snapshot.add(builder.build(), report);

        var result = PersistedReferenceResolver.resolve(
                new PersistedDefinitionPointer(
                        FIXTURES.id(),
                        definition.id()
                ),
                FIXTURES,
                snapshot.build(),
                MissingDefinitionPolicy.FAIL
        );

        assertEquals(
                PersistedReferenceStatus.RESOLVED,
                result.status()
        );
        assertEquals(definition, result.definition().orElseThrow());
    }

    @Test
    void missingReferenceUsesExplicitPolicy() {
        RegistrySnapshot.Builder snapshot =
                RegistrySnapshot.builder();
        ValidationReport report = new ValidationReport();
        snapshot.add(
                new DefinitionRegistryBuilder<>(FIXTURES).build(),
                report
        );

        PersistedDefinitionPointer pointer =
                new PersistedDefinitionPointer(
                        FIXTURES.id(),
                        RpgId.parse("world_rpg:test/missing")
                );

        assertThrows(
                IllegalStateException.class,
                () -> PersistedReferenceResolver.resolve(
                        pointer,
                        FIXTURES,
                        snapshot.build(),
                        MissingDefinitionPolicy.FAIL
                )
        );

        assertEquals(
                PersistedReferenceStatus.PRESERVED_UNRESOLVED,
                PersistedReferenceResolver.resolve(
                        pointer,
                        FIXTURES,
                        snapshot.build(),
                        MissingDefinitionPolicy.PRESERVE_UNRESOLVED
                ).status()
        );

        assertEquals(
                PersistedReferenceStatus.DROPPED,
                PersistedReferenceResolver.resolve(
                        pointer,
                        FIXTURES,
                        snapshot.build(),
                        MissingDefinitionPolicy.DROP
                ).status()
        );
    }

    private record Fixture(RpgId id) implements RpgDefinition {
    }
}
