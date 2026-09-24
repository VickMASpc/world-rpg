package dev.worldrpg.content.load;

import dev.worldrpg.api.id.RpgId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefinitionDomainCatalog {
    private final Map<RpgId, DefinitionDomainHandler<?>> handlers;
    private final List<CrossRegistryValidator> crossRegistryValidators;
    private final Map<RpgId, DefinitionReloadGuard> reloadGuards;
    private final Map<RpgId, DefinitionSchemaMigrator> schemaMigrators;

    private DefinitionDomainCatalog(
            Map<RpgId, DefinitionDomainHandler<?>> handlers,
            List<CrossRegistryValidator> crossRegistryValidators,
            Map<RpgId, DefinitionReloadGuard> reloadGuards,
            Map<RpgId, DefinitionSchemaMigrator> schemaMigrators
    ) {
        this.handlers = Collections.unmodifiableMap(
                new LinkedHashMap<>(handlers)
        );
        this.crossRegistryValidators =
                List.copyOf(crossRegistryValidators);
        this.reloadGuards = Collections.unmodifiableMap(
                new LinkedHashMap<>(reloadGuards)
        );
        this.schemaMigrators = Collections.unmodifiableMap(
                new LinkedHashMap<>(schemaMigrators)
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<DefinitionDomainHandler<?>> find(RpgId registryId) {
        return Optional.ofNullable(
                handlers.get(
                        Objects.requireNonNull(registryId, "registryId")
                )
        );
    }

    public Collection<DefinitionDomainHandler<?>> handlers() {
        return handlers.values();
    }

    public List<CrossRegistryValidator> crossRegistryValidators() {
        return crossRegistryValidators;
    }

    public Optional<DefinitionReloadGuard> reloadGuard(
            RpgId registryId
    ) {
        return Optional.ofNullable(
                reloadGuards.get(
                        Objects.requireNonNull(registryId, "registryId")
                )
        );
    }

    public Optional<DefinitionSchemaMigrator> schemaMigrator(
            RpgId registryId
    ) {
        return Optional.ofNullable(
                schemaMigrators.get(
                        Objects.requireNonNull(registryId, "registryId")
                )
        );
    }

    public static final class Builder {
        private final Map<RpgId, DefinitionDomainHandler<?>> handlers =
                new LinkedHashMap<>();
        private final List<CrossRegistryValidator> crossRegistryValidators =
                new ArrayList<>();
        private final Map<RpgId, DefinitionReloadGuard> reloadGuards =
                new LinkedHashMap<>();
        private final Map<RpgId, List<DefinitionSchemaMigration>>
                schemaMigrations = new LinkedHashMap<>();

        public Builder add(DefinitionDomainHandler<?> handler) {
            Objects.requireNonNull(handler, "handler");
            RpgId registryId =
                    handler.domain().registryKey().id();

            if (handlers.putIfAbsent(registryId, handler) != null) {
                throw new IllegalArgumentException(
                        "Duplicate definition domain: " + registryId
                );
            }
            return this;
        }

        public Builder addCrossRegistryValidator(
                CrossRegistryValidator validator
        ) {
            crossRegistryValidators.add(
                    Objects.requireNonNull(validator, "validator")
            );
            return this;
        }

        public Builder addReloadGuard(
                RpgId registryId,
                DefinitionReloadGuard guard
        ) {
            Objects.requireNonNull(registryId, "registryId");
            Objects.requireNonNull(guard, "guard");

            if (reloadGuards.putIfAbsent(registryId, guard) != null) {
                throw new IllegalArgumentException(
                        "Duplicate reload guard: " + registryId
                );
            }
            return this;
        }

        public Builder addSchemaMigration(
                RpgId registryId,
                DefinitionSchemaMigration migration
        ) {
            schemaMigrations.computeIfAbsent(
                    Objects.requireNonNull(registryId, "registryId"),
                    ignored -> new ArrayList<>()
            ).add(Objects.requireNonNull(migration, "migration"));
            return this;
        }

        public DefinitionDomainCatalog build() {
            for (RpgId registryId : reloadGuards.keySet()) {
                requireKnownDomain(registryId, "Reload guard");
            }

            Map<RpgId, DefinitionSchemaMigrator> migrators =
                    new LinkedHashMap<>();

            schemaMigrations.forEach((registryId, migrations) -> {
                requireKnownDomain(registryId, "Schema migration");
                migrators.put(
                        registryId,
                        new DefinitionSchemaMigrator(migrations)
                );
            });

            return new DefinitionDomainCatalog(
                    handlers,
                    crossRegistryValidators,
                    reloadGuards,
                    migrators
            );
        }

        private void requireKnownDomain(
                RpgId registryId,
                String owner
        ) {
            if (!handlers.containsKey(registryId)) {
                throw new IllegalArgumentException(
                        owner
                                + " targets unknown definition domain: "
                                + registryId
                );
            }
        }
    }
}
