package dev.worldrpg.combat.cooldown;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class CooldownBook {
    public static final CooldownKey GLOBAL =
            CooldownKey.of("world_rpg:cooldown/global");

    private final Map<CooldownKey, Long> readyAtTicks = new LinkedHashMap<>();

    public boolean isReady(CooldownKey key, long gameTick) {
        Objects.requireNonNull(key, "key");
        requireTick(gameTick);

        return gameTick >= readyAtTicks.getOrDefault(key, 0L);
    }

    public long readyAt(CooldownKey key) {
        return readyAtTicks.getOrDefault(
                Objects.requireNonNull(key, "key"),
                0L
        );
    }

    public long remainingTicks(CooldownKey key, long gameTick) {
        requireTick(gameTick);
        return Math.max(0L, readyAt(key) - gameTick);
    }

    public long start(CooldownKey key, long durationTicks, long gameTick) {
        Objects.requireNonNull(key, "key");
        requireTick(gameTick);

        if (durationTicks < 0) {
            throw new IllegalArgumentException("cooldown duration must be >= 0");
        }

        long readyAt = safeAdd(gameTick, durationTicks);

        if (durationTicks == 0) {
            readyAtTicks.remove(key);
        } else {
            readyAtTicks.put(key, readyAt);
        }

        return readyAt;
    }

    public void clear(CooldownKey key) {
        readyAtTicks.remove(Objects.requireNonNull(key, "key"));
    }

    public Map<CooldownKey, Long> activeAt(long gameTick) {
        requireTick(gameTick);

        Map<CooldownKey, Long> active = new LinkedHashMap<>();
        readyAtTicks.forEach((key, readyAt) -> {
            if (readyAt > gameTick) {
                active.put(key, readyAt);
            }
        });

        return Collections.unmodifiableMap(active);
    }

    private static long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static void requireTick(long gameTick) {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }
}
