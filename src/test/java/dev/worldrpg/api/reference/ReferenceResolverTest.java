package dev.worldrpg.api.reference;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.DefinitionRegistryBuilder;
import dev.worldrpg.api.registry.RegistryKey;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceResolverTest {
    private static final RegistryKey<FixtureDefinition> FIXTURES = new RegistryKey<>(
            RpgId.parse("world_rpg:registry/reference_fixtures"),
            FixtureDefinition.class
    );

    @Test
    void requiredReferenceResolvesTypedDefinition() {
        RpgId targetId = RpgId.parse("world_rpg:test/target");
        RegistrySnapshot snapshot = snapshotWith(targetId);
        ValidationReport report = new ValidationReport();

        var resolved = ReferenceResolver.resolve(
                new RequiredDefinitionRef<>(FIXTURES, targetId),
                snapshot,
                report,
                SourceRef.of("source.json")
        );

        assertTrue(resolved.isPresent());
        assertEquals("target", resolved.get().value());
        assertFalse(report.hasErrors());
    }

    @Test
    void missingRequiredReferenceIsAnError() {
        RegistrySnapshot snapshot = snapshotWith(RpgId.parse("world_rpg:test/existing"));
        ValidationReport report = new ValidationReport();

        var resolved = ReferenceResolver.resolve(
                new RequiredDefinitionRef<>(FIXTURES, RpgId.parse("world_rpg:test/missing")),
                snapshot,
                report,
                SourceRef.of("source.json")
        );

        assertTrue(resolved.isEmpty());
        assertTrue(report.hasErrors());
    }

    @Test
    void absentOptionalReferenceIsValidButPresentMissingTargetIsNot() {
        RegistrySnapshot snapshot = snapshotWith(RpgId.parse("world_rpg:test/existing"));

        ValidationReport absentReport = new ValidationReport();
        assertTrue(ReferenceResolver.resolve(
                OptionalDefinitionRef.absent(FIXTURES),
                snapshot,
                absentReport,
                SourceRef.of("source.json")
        ).isEmpty());
        assertFalse(absentReport.hasErrors());

        ValidationReport missingReport = new ValidationReport();
        assertTrue(ReferenceResolver.resolve(
                OptionalDefinitionRef.of(FIXTURES, RpgId.parse("world_rpg:test/missing")),
                snapshot,
                missingReport,
                SourceRef.of("source.json")
        ).isEmpty());
        assertTrue(missingReport.hasErrors());
    }

    private static RegistrySnapshot snapshotWith(RpgId id) {
        ValidationReport report = new ValidationReport();
        DefinitionRegistryBuilder<FixtureDefinition> registryBuilder =
                new DefinitionRegistryBuilder<>(FIXTURES);

        registryBuilder.add(
                new FixtureDefinition(id, id.path().endsWith("target") ? "target" : "existing"),
                SourceRef.of("fixture.json"),
                report
        );

        RegistrySnapshot.Builder snapshotBuilder = RegistrySnapshot.builder();
        snapshotBuilder.add(registryBuilder.build(), report);
        return snapshotBuilder.build();
    }

    private record FixtureDefinition(RpgId id, String value) implements RpgDefinition {
    }
}
