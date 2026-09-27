package dev.worldrpg.player;

import dev.worldrpg.api.id.RpgId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PlayerRpgInventory {
    private final Map<RpgId, Integer> itemQuantities = new LinkedHashMap<>();
    private long copper;

    public PlayerRpgInventory() {
    }

    public PlayerRpgInventory(
            Map<RpgId, Integer> itemQuantities,
            long copper
    ) {
        if (copper < 0) {
            throw new IllegalArgumentException("copper must be >= 0");
        }
        this.copper = copper;
        Objects.requireNonNull(itemQuantities, "itemQuantities")
                .forEach((itemId, quantity) -> {
                    Objects.requireNonNull(itemId, "itemId");
                    if (quantity < 1) {
                        throw new IllegalArgumentException(
                                "item quantity must be >= 1 for " + itemId
                        );
                    }
                    this.itemQuantities.put(itemId, quantity);
                });
    }

    public long copper() {
        return copper;
    }

    public void addCopper(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("copper reward must be >= 0");
        }
        copper = Math.addExact(copper, amount);
    }

    public void grantItem(RpgId itemId, int quantity) {
        Objects.requireNonNull(itemId, "itemId");
        if (quantity < 1) {
            throw new IllegalArgumentException("item grant quantity must be >= 1");
        }
        itemQuantities.merge(itemId, quantity, Math::addExact);
    }

    public int quantity(RpgId itemId) {
        return itemQuantities.getOrDefault(
                Objects.requireNonNull(itemId, "itemId"),
                0
        );
    }

    public Map<RpgId, Integer> itemQuantities() {
        return Map.copyOf(itemQuantities);
    }

    public int distinctItemCount() {
        return itemQuantities.size();
    }

    public long totalItemCount() {
        return itemQuantities.values().stream()
                .mapToLong(Integer::longValue)
                .sum();
    }
}
