package dev.worldrpg.combat.aura;

import java.util.Objects;

public record AuraRemoval(
        AuraInstance instance,
        AuraRemovalReason reason
) {
    public AuraRemoval {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(reason, "reason");
    }
}
