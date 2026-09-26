package dev.worldrpg.combat.math;

/**
 * First inspectable P4 coefficient conventions for spell-like effects.
 *
 * <p>These are calibration helpers, not a rule that all authored abilities
 * must derive their coefficient from cast time. Authored effects still carry
 * the resolved explicit coefficient.</p>
 */
public final class WorldRpgCoefficientDraft {
    public static final long FULL_SPELL_COEFFICIENT_TICKS =
            70L;
    public static final long INSTANT_EQUIVALENT_TICKS =
            30L;

    private WorldRpgCoefficientDraft() {
    }

    /**
     * Direct cast coefficient reaches 100% at 3.5 seconds and is capped there.
     */
    public static double directSpell(
            long castDurationTicks
    ) {
        if (castDurationTicks < 1L) {
            throw new IllegalArgumentException(
                    "castDurationTicks must be >= 1"
            );
        }

        return Math.min(
                1.0,
                (double) castDurationTicks
                        / (double) FULL_SPELL_COEFFICIENT_TICKS
        );
    }

    /**
     * Draft coefficient for instant spell-like effects.
     */
    public static double instantSpell() {
        return (double) INSTANT_EQUIVALENT_TICKS
                / (double) FULL_SPELL_COEFFICIENT_TICKS;
    }

    /**
     * Total coefficient budget for one full channel.
     */
    public static double channelTotal(
            long channelDurationTicks
    ) {
        return directSpell(channelDurationTicks);
    }

    /**
     * Equal-share coefficient for one tick of a fixed-count channel.
     */
    public static double channelTick(
            long channelDurationTicks,
            int tickCount
    ) {
        if (tickCount < 1) {
            throw new IllegalArgumentException(
                    "tickCount must be >= 1"
            );
        }

        return channelTotal(channelDurationTicks)
                / (double) tickCount;
    }
}
