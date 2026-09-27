package dev.worldrpg.sim;

/**
 * Deterministic headless simulation clock.
 *
 * <p>Advancing time has no gameplay side effects by itself.</p>
 */
public final class SimulationClock {
    private long tick;

    public SimulationClock() {
        this(0L);
    }

    public SimulationClock(long startTick) {
        if (startTick < 0) {
            throw new IllegalArgumentException(
                    "startTick must be >= 0"
            );
        }
        tick = startTick;
    }

    public long now() {
        return tick;
    }

    public long advance(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException(
                    "ticks must be >= 0"
            );
        }

        tick = Math.addExact(tick, ticks);
        return tick;
    }
}
