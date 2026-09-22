package dev.worldrpg.network.p3;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-side replay/duplicate guard for client activation intent.
 *
 * <p>Sequences are scoped to one player connection/server lifetime. They are
 * protocol state, not persisted character state.</p>
 */
public final class P3RequestSequenceTracker {
    private final Map<UUID, Long> lastAccepted = new HashMap<>();

    public boolean accept(UUID playerUuid, long sequence) {
        Objects.requireNonNull(playerUuid, "playerUuid");

        if (sequence < 0) {
            return false;
        }

        Long previous = lastAccepted.get(playerUuid);
        if (previous != null && sequence <= previous) {
            return false;
        }

        lastAccepted.put(playerUuid, sequence);
        return true;
    }

    public void remove(UUID playerUuid) {
        lastAccepted.remove(Objects.requireNonNull(playerUuid, "playerUuid"));
    }

    public void clear() {
        lastAccepted.clear();
    }
}
