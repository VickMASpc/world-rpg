package dev.worldrpg.combat.aura;

import java.util.Objects;

public record AuraApplicationResult(
        AuraInstance instance,
        boolean created,
        int previousStacks,
        int currentStacks
) {
    public AuraApplicationResult {
        Objects.requireNonNull(instance, "instance");
    }
}
