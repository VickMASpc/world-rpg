package dev.worldrpg.content.load;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.registry.DefinitionRegistry;
import dev.worldrpg.api.registry.DefinitionRegistryBuilder;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.api.registry.RegistrySnapshot;
import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.decode.DecodedJsonDocument;
import dev.worldrpg.content.decode.JsonDocumentDecoder;
import dev.worldrpg.content.source.ContentSource;
import dev.worldrpg.content.source.ContentSourceSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Executes the P2 candidate-load transaction.
 */
public final class ContentLoader {
    private final DefinitionDomainCatalog catalog;
    private final RegistryPublisher publisher;

    public ContentLoader(
            DefinitionDomainCatalog catalog,
            RegistryPublisher publisher
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    public ContentLoadResult loadAndPublish(Iterable<ContentSource> inputSources) {
        ValidationReport report = new ValidationReport();
        ContentSourceSet sourceSet = ContentSourceSet.create(inputSources, report);

        Map<dev.worldrpg.api.id.RpgId, DomainAccumulator<?>> accumulators =
                createAccumulators();

        for (ContentSource source : sourceSet.sources()) {
            Optional<DecodedJsonDocument> decoded =
                    JsonDocumentDecoder.decode(source, report);

            if (decoded.isEmpty()) {
                continue;
            }

            DecodedJsonDocument document = decoded.get();
            DomainAccumulator<?> accumulator =
                    accumulators.get(document.header().registry());

            if (accumulator == null) {
                report.error(
                        "content.unknown_registry",
                        "No definition domain is registered for "
                                + document.header().registry(),
                        source.sourceRef(),
                        document.header().id()
                );
                continue;
            }

            accumulator.decodeAndAdd(document, report);
        }

        RegistrySnapshot.Builder snapshotBuilder = RegistrySnapshot.builder();
        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.addRegistryTo(snapshotBuilder, report);
        }
        RegistrySnapshot candidate = snapshotBuilder.build();

        // Stage: reference resolution against the complete candidate snapshot.
        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.resolveReferences(candidate, report);
        }

        // Stage: per-domain semantic validation.
        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.validateDefinitions(candidate, report);
        }

        // Stage: whole-snapshot/cross-registry validation.
        for (CrossRegistryValidator validator : catalog.crossRegistryValidators()) {
            validator.validate(candidate, report);
        }

        boolean published = publisher.publishIfValid(candidate, report);
        return new ContentLoadResult(candidate, report, published);
    }

    private Map<dev.worldrpg.api.id.RpgId, DomainAccumulator<?>> createAccumulators() {
        Map<dev.worldrpg.api.id.RpgId, DomainAccumulator<?>> accumulators =
                new LinkedHashMap<>();

        for (DefinitionDomainHandler<?> handler : catalog.handlers()) {
            accumulators.put(
                    handler.domain().registryKey().id(),
                    createAccumulator(handler)
            );
        }

        return accumulators;
    }

    private static <T extends RpgDefinition> DomainAccumulator<T> createAccumulator(
            DefinitionDomainHandler<T> handler
    ) {
        return new DomainAccumulator<>(handler);
    }

    private static final class DomainAccumulator<T extends RpgDefinition> {
        private final DefinitionDomainHandler<T> handler;
        private final DefinitionRegistryBuilder<T> registryBuilder;
        private final Map<dev.worldrpg.api.id.RpgId, LoadedDefinition<T>> loaded =
                new LinkedHashMap<>();

        private DomainAccumulator(DefinitionDomainHandler<T> handler) {
            this.handler = handler;
            this.registryBuilder =
                    new DefinitionRegistryBuilder<>(handler.domain().registryKey());
        }

        private void decodeAndAdd(
                DecodedJsonDocument document,
                ValidationReport report
        ) {
            int sourceSchema = document.header().schema().value();
            int currentSchema = handler.domain().currentSchema().value();

            if (sourceSchema < currentSchema) {
                report.error(
                        "schema.migration_required",
                        "Definition schema " + sourceSchema
                                + " is older than current schema " + currentSchema
                                + " for " + handler.domain().registryKey().id()
                                + "; no migration has been registered yet",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return;
            }

            if (sourceSchema > currentSchema) {
                report.error(
                        "schema.unsupported_newer",
                        "Definition schema " + sourceSchema
                                + " is newer than supported schema " + currentSchema
                                + " for " + handler.domain().registryKey().id(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return;
            }

            Optional<T> definition = handler.decoder().decode(document, report);
            if (definition.isEmpty()) {
                return;
            }

            T value = definition.get();
            if (!value.id().equals(document.header().id())) {
                report.error(
                        "content.decoder_id_mismatch",
                        "Domain decoder returned ID " + value.id()
                                + " but document header declares " + document.header().id(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return;
            }

            if (registryBuilder.add(value, document.source().sourceRef(), report)) {
                loaded.put(
                        value.id(),
                        new LoadedDefinition<>(value, document.source().sourceRef())
                );
            }
        }

        private void addRegistryTo(
                RegistrySnapshot.Builder snapshotBuilder,
                ValidationReport report
        ) {
            DefinitionRegistry<T> registry = registryBuilder.build();
            snapshotBuilder.add(registry, report);
        }

        private void resolveReferences(
                RegistrySnapshot candidate,
                ValidationReport report
        ) {
            for (LoadedDefinition<T> entry : loaded.values()) {
                handler.resolver().resolve(
                        entry.definition(),
                        candidate,
                        entry.source(),
                        report
                );
            }
        }

        private void validateDefinitions(
                RegistrySnapshot candidate,
                ValidationReport report
        ) {
            for (LoadedDefinition<T> entry : loaded.values()) {
                handler.validator().validate(
                        entry.definition(),
                        candidate,
                        entry.source(),
                        report
                );
            }
        }
    }

    private record LoadedDefinition<T extends RpgDefinition>(
            T definition,
            SourceRef source
    ) {
    }
}
