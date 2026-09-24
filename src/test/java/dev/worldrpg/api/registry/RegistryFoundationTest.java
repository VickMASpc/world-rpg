package dev.worldrpg.api.registry;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryFoundationTest {
    private static final RegistryKey<FixtureDefinition> FIXTURES = new RegistryKey<>(
            RpgId.parse("world_rpg:registry/test_fixtures"),
            FixtureDefinition.class
    );

    @Test
    void duplicateDefinitionIsRejectedWithoutOverwritingFirstDefinition() {
        ValidationReport report = new ValidationReport();
        DefinitionRegistryBuilder<FixtureDefinition> builder = new DefinitionRegistryBuilder<>(FIXTURES);

        RpgId id = RpgId.parse("world_rpg:test/example");

        assertTrue(builder.add(
                new FixtureDefinition(id, "first"),
                SourceRef.of("first.json"),
                report
        ));
        assertFalse(builder.add(
                new FixtureDefinition(id, "second"),
                SourceRef.of("second.json"),
                report
        ));

        DefinitionRegistry<FixtureDefinition> registry = builder.build();

        assertEquals("first", registry.require(id).value());
        assertEquals(1, registry.size());
        assertEquals(1, report.errorCount());
    }

    @Test
    void publishedRegistryIsImmutableAndTyped() {
        ValidationReport report = new ValidationReport();
        DefinitionRegistryBuilder<FixtureDefinition> builder = new DefinitionRegistryBuilder<>(FIXTURES);
        RpgId id = RpgId.parse("world_rpg:test/example");

        builder.add(new FixtureDefinition(id, "value"), SourceRef.of("fixture.json"), report);
        DefinitionRegistry<FixtureDefinition> registry = builder.build();

        RegistrySnapshot.Builder snapshotBuilder = RegistrySnapshot.builder();
        assertTrue(snapshotBuilder.add(registry, report));
        RegistrySnapshot snapshot = snapshotBuilder.build();

        assertEquals("value", snapshot.require(FIXTURES).require(id).value());
        assertThrows(
                UnsupportedOperationException.class,
                () -> registry.asMap().put(id, new FixtureDefinition(id, "mutated"))
        );
    }

    @Test
    void invalidCandidateNeverReplacesLastKnownGoodSnapshot() {
        RegistryPublisher publisher = new RegistryPublisher();

        ValidationReport goodReport = new ValidationReport();
        DefinitionRegistryBuilder<FixtureDefinition> goodBuilder = new DefinitionRegistryBuilder<>(FIXTURES);
        RpgId goodId = RpgId.parse("world_rpg:test/good");
        goodBuilder.add(new FixtureDefinition(goodId, "good"), SourceRef.of("good.json"), goodReport);

        RegistrySnapshot.Builder goodSnapshotBuilder = RegistrySnapshot.builder();
        goodSnapshotBuilder.add(goodBuilder.build(), goodReport);
        RegistrySnapshot goodSnapshot = goodSnapshotBuilder.build();

        assertTrue(publisher.publishIfValid(goodSnapshot, goodReport));
        assertSame(goodSnapshot, publisher.active());

        ValidationReport badReport = new ValidationReport();
        DefinitionRegistryBuilder<FixtureDefinition> badBuilder = new DefinitionRegistryBuilder<>(FIXTURES);
        RpgId duplicate = RpgId.parse("world_rpg:test/duplicate");
        badBuilder.add(new FixtureDefinition(duplicate, "one"), SourceRef.of("one.json"), badReport);
        badBuilder.add(new FixtureDefinition(duplicate, "two"), SourceRef.of("two.json"), badReport);

        RegistrySnapshot.Builder badSnapshotBuilder = RegistrySnapshot.builder();
        badSnapshotBuilder.add(badBuilder.build(), badReport);
        RegistrySnapshot badSnapshot = badSnapshotBuilder.build();

        assertTrue(badReport.hasErrors());
        assertFalse(publisher.publishIfValid(badSnapshot, badReport));
        assertSame(goodSnapshot, publisher.active());
    }

    private record FixtureDefinition(RpgId id, String value) implements RpgDefinition {
    }
}
