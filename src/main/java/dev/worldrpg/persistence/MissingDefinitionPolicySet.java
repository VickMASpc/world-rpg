package dev.worldrpg.persistence;

import dev.worldrpg.api.id.RpgId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class MissingDefinitionPolicySet {
    private final MissingDefinitionPolicy defaultPolicy;
    private final Map<RpgId, MissingDefinitionPolicy> byRegistry;

    public MissingDefinitionPolicySet(
            MissingDefinitionPolicy defaultPolicy,
            Map<RpgId, MissingDefinitionPolicy> byRegistry
    ) {
        this.defaultPolicy =
                Objects.requireNonNull(defaultPolicy, "defaultPolicy");
        this.byRegistry = Map.copyOf(
                new LinkedHashMap<>(
                        Objects.requireNonNull(byRegistry, "byRegistry")
                )
        );
    }

    public static MissingDefinitionPolicySet failByDefault() {
        return new MissingDefinitionPolicySet(
                MissingDefinitionPolicy.FAIL,
                Map.of()
        );
    }

    public MissingDefinitionPolicy policyFor(RpgId registryId) {
        return byRegistry.getOrDefault(
                Objects.requireNonNull(registryId, "registryId"),
                defaultPolicy
        );
    }
}
