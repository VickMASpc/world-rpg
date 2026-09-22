package dev.worldrpg.content.load;

import dev.worldrpg.api.data.ReloadSafety;
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
import dev.worldrpg.content.source.ContentSourceBatch;
import dev.worldrpg.content.source.ContentSourceSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ContentLoader {
    private static final SourceRef RELOAD_SOURCE =
            SourceRef.of("<reload-safety>");

    private final DefinitionDomainCatalog catalog;
    private final RegistryPublisher publisher;

    public ContentLoader(
            DefinitionDomainCatalog catalog,
            RegistryPublisher publisher
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    public ContentLoadResult loadAndPublish(
            Iterable<ContentSource> inputSources
    ) {
        return loadAndPublish(
                ContentSourceBatch.clean(inputSources)
        );
    }

    public ContentLoadResult loadAndPublish(
            ContentSourceBatch batch
    ) {
        Objects.requireNonNull(batch, "batch");

        ValidationReport report = new ValidationReport();
        report.merge(batch.diagnostics());

        ContentSourceSet sourceSet =
                ContentSourceSet.create(batch.sources(), report);

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

            DefinitionSchemaMigrator migrator =
                    catalog.schemaMigrator(
                            document.header().registry()
                    ).orElseGet(() ->
                            new DefinitionSchemaMigrator(java.util.List.of())
                    );

            accumulator.decodeAndAdd(
                    document,
                    migrator,
                    report
            );
        }

        RegistrySnapshot.Builder snapshotBuilder =
                RegistrySnapshot.builder();
        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.addRegistryTo(snapshotBuilder, report);
        }
        RegistrySnapshot candidate = snapshotBuilder.build();

        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.resolveReferences(candidate, report);
        }

        for (DomainAccumulator<?> accumulator : accumulators.values()) {
            accumulator.validateDefinitions(candidate, report);
        }

        for (CrossRegistryValidator validator :
                catalog.crossRegistryValidators()) {
            validator.validate(candidate, report);
        }

        validateReloadSafety(candidate, report);

        boolean published =
                publisher.publishIfValid(candidate, report);

        return new ContentLoadResult(
                candidate,
                report,
                published
        );
    }

    private void validateReloadSafety(
            RegistrySnapshot candidate,
            ValidationReport report
    ) {
        RegistrySnapshot active = publisher.active();

        if (active.registryCount() == 0) {
            return;
        }

        for (DefinitionDomainHandler<?> handler : catalog.handlers()) {
            var domain = handler.domain();
            var key = domain.registryKey();

            DefinitionRegistry<?> candidateRegistry =
                    candidate.registries().get(key.id());
            DefinitionRegistry<?> activeRegistry =
                    active.registries().get(key.id());

            boolean changed = activeRegistry == null
                    || candidateRegistry == null
                    || !activeRegistry.asMap().equals(
                            candidateRegistry.asMap()
                    );

            if (!changed) {
                continue;
            }

            if (domain.reloadSafety() == ReloadSafety.SAFE) {
                continue;
            }

            if (domain.reloadSafety() == ReloadSafety.RESTART) {
                report.error(
                        "reload.restart_required",
                        "Definition domain "
                                + key.id()
                                + " changed but requires restart/save reload",
                        RELOAD_SOURCE
                );
                continue;
            }

            Optional<DefinitionReloadGuard> guard =
                    catalog.reloadGuard(key.id());

            if (guard.isEmpty()) {
                report.error(
                        "reload.guard_required",
                        "Definition domain "
                                + key.id()
                                + " changed but has no live reload guard",
                        RELOAD_SOURCE
                );
                continue;
            }

            guard.get().validate(
                    activeRegistry,
                    candidateRegistry,
                    report
            );
        }
    }

    private Map<dev.worldrpg.api.id.RpgId, DomainAccumulator<?>>
    createAccumulators() {
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

    private static <T extends RpgDefinition>
    DomainAccumulator<T> createAccumulator(
            DefinitionDomainHandler<T> handler
    ) {
        return new DomainAccumulator<>(handler);
    }

    private static final class DomainAccumulator<T extends RpgDefinition> {
        private final DefinitionDomainHandler<T> handler;
        private final DefinitionRegistryBuilder<T> registryBuilder;
        private final Map<dev.worldrpg.api.id.RpgId, LoadedDefinition<T>>
                loaded = new LinkedHashMap<>();

        private DomainAccumulator(
                DefinitionDomainHandler<T> handler
        ) {
            this.handler = handler;
            this.registryBuilder =
                    new DefinitionRegistryBuilder<>(
                            handler.domain().registryKey()
                    );
        }

        private void decodeAndAdd(
                DecodedJsonDocument document,
                DefinitionSchemaMigrator schemaMigrator,
                ValidationReport report
        ) {
            int sourceSchema = document.header().schema().value();
            int currentSchema =
                    handler.domain().currentSchema().value();

            if (sourceSchema > currentSchema) {
                report.error(
                        "schema.unsupported_newer",
                        "Definition schema "
                                + sourceSchema
                                + " is newer than supported schema "
                                + currentSchema
                                + " for "
                                + handler.domain().registryKey().id(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return;
            }

            if (sourceSchema < currentSchema) {
                Optional<DecodedJsonDocument> migrated =
                        schemaMigrator.migrate(
                                document,
                                handler.domain().currentSchema(),
                                report
                        );

                if (migrated.isEmpty()) {
                    return;
                }

                document = migrated.get();
            }

            Optional<T> definition =
                    handler.decoder().decode(document, report);
            if (definition.isEmpty()) {
                return;
            }

            T value = definition.get();

            if (!value.id().equals(document.header().id())) {
                report.error(
                        "content.decoder_id_mismatch",
                        "Domain decoder returned ID "
                                + value.id()
                                + " but document header declares "
                                + document.header().id(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return;
            }

            if (registryBuilder.add(
                    value,
                    document.source().sourceRef(),
                    report
            )) {
                loaded.put(
                        value.id(),
                        new LoadedDefinition<>(
                                value,
                                document.source().sourceRef()
                        )
                );
            }
        }

        private void addRegistryTo(
                RegistrySnapshot.Builder snapshotBuilder,
                ValidationReport report
        ) {
            DefinitionRegistry<T> registry =
                    registryBuilder.build();
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
