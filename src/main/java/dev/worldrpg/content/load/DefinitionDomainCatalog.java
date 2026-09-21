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

/**
 * Immutable catalog of code-registered definition domains and global validators.
 */
public final class DefinitionDomainCatalog {
    private final Map<RpgId, DefinitionDomainHandler<?>> handlers;
    private final List<CrossRegistryValidator> crossRegistryValidators;

    private DefinitionDomainCatalog(
            Map<RpgId, DefinitionDomainHandler<?>> handlers,
            List<CrossRegistryValidator> crossRegistryValidators
    ) {
        this.handlers = Collections.unmodifiableMap(new LinkedHashMap<>(handlers));
        this.crossRegistryValidators = List.copyOf(crossRegistryValidators);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<DefinitionDomainHandler<?>> find(RpgId registryId) {
        return Optional.ofNullable(handlers.get(Objects.requireNonNull(registryId, "registryId")));
    }

    public Collection<DefinitionDomainHandler<?>> handlers() {
        return handlers.values();
    }

    public List<CrossRegistryValidator> crossRegistryValidators() {
        return crossRegistryValidators;
    }

    public static final class Builder {
        private final Map<RpgId, DefinitionDomainHandler<?>> handlers = new LinkedHashMap<>();
        private final List<CrossRegistryValidator> crossRegistryValidators = new ArrayList<>();

        public Builder add(DefinitionDomainHandler<?> handler) {
            Objects.requireNonNull(handler, "handler");
            RpgId registryId = handler.domain().registryKey().id();

            DefinitionDomainHandler<?> previous = handlers.putIfAbsent(registryId, handler);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate definition domain: " + registryId);
            }
            return this;
        }

        public Builder addCrossRegistryValidator(CrossRegistryValidator validator) {
            crossRegistryValidators.add(Objects.requireNonNull(validator, "validator"));
            return this;
        }

        public DefinitionDomainCatalog build() {
            return new DefinitionDomainCatalog(handlers, crossRegistryValidators);
        }
    }
}
