package dev.worldrpg.combat.ability;

import dev.worldrpg.combat.resource.ResourceKey;

import java.util.Objects;

public record AbilityCost(ResourceKey resource, double amount) {
    public AbilityCost {
        Objects.requireNonNull(resource, "resource");

        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("ability cost must be finite and >= 0");
        }
    }
}
