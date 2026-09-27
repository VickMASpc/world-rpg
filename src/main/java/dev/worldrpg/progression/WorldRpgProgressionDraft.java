package dev.worldrpg.progression;

/**
 * Current P4 calibration targets.
 *
 * <p>These values are intentionally not called final. They turn the accepted
 * 1-100 / 350-500 hour doctrine into something simulators can measure while
 * content density and reward throughput are still unknown.</p>
 */
public final class WorldRpgProgressionDraft {
    public static final ProgressionPaceProfile FIRST_CHARACTER =
            new ProgressionPaceProfile(
                    100,
                    0.75,
                    6.50,
                    0.60
            );

    private WorldRpgProgressionDraft() {
    }
}
