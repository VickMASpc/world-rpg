package dev.worldrpg.content.adventure;

import dev.worldrpg.combat.stat.StatKey;

import java.util.Objects;

public record EquipmentStatSpec(
        StatKey stat,
        double amount
) {
    public EquipmentStatSpec {
        Objects.requireNonNull(stat, "stat");
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException(
                    "equipment stat amount must be finite"
            );
        }
    }
}
