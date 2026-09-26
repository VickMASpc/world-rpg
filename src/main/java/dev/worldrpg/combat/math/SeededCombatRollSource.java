package dev.worldrpg.combat.math;

/**
 * Small explicitly specified SplitMix64 stream.
 *
 * <p>The algorithm lives in World RPG code so deterministic simulator fixtures
 * do not depend on an external/JDK RNG implementation contract.</p>
 */
public final class SeededCombatRollSource
        implements CombatRollSource {
    private static final long GAMMA =
            0x9E3779B97F4A7C15L;
    private static final double UNIT_SCALE =
            0x1.0p-53;

    private long state;
    private long rollsConsumed;

    public SeededCombatRollSource(long seed) {
        state = seed;
    }

    @Override
    public double nextUnit() {
        long value = nextLong();
        return (value >>> 11) * UNIT_SCALE;
    }

    public long rollsConsumed() {
        return rollsConsumed;
    }

    private long nextLong() {
        long z = state += GAMMA;
        z = (z ^ (z >>> 30))
                * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27))
                * 0x94D049BB133111EBL;
        rollsConsumed++;
        return z ^ (z >>> 31);
    }
}
