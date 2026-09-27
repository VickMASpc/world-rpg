package dev.worldrpg.content.adventure;

import dev.worldrpg.player.EquipmentSlot;

import java.util.List;
import java.util.Objects;

public record EquipmentSpec(
        EquipmentSlot slot,
        List<EquipmentStatSpec> stats
) {
    public EquipmentSpec {
        Objects.requireNonNull(slot, "slot");
        stats = List.copyOf(
                Objects.requireNonNull(stats, "stats")
        );
        if (stats.isEmpty()) {
            throw new IllegalArgumentException(
                    "equipment must provide at least one stat"
            );
        }
    }
}
