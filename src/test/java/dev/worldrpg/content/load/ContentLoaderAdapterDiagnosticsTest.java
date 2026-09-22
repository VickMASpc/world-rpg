package dev.worldrpg.content.load;

import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSourceBatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentLoaderAdapterDiagnosticsTest {
    @Test
    void adapterErrorBlocksPublicationAndPreservesLastKnownGood() {
        RegistryPublisher publisher = new RegistryPublisher();
        DefinitionDomainCatalog catalog =
                DefinitionDomainCatalog.builder().build();
        ContentLoader loader = new ContentLoader(catalog, publisher);

        var lastKnownGood = publisher.active();

        ValidationReport adapterDiagnostics =
                new ValidationReport();
        adapterDiagnostics.error(
                "source.io",
                "fixture read failure",
                SourceRef.of("pack=test resource=test:broken.json")
        );

        ContentLoadResult result = loader.loadAndPublish(
                new ContentSourceBatch(
                        List.of(),
                        adapterDiagnostics
                )
        );

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertSame(lastKnownGood, publisher.active());
    }
}
