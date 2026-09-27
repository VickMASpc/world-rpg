package dev.worldrpg.progression;

/**
 * Reference-actor P4 calibration curves.
 *
 * <p>These are not class templates and do not include gear/talent multipliers.
 * They exist so simulator scenarios have restrained level-scaled magnitudes to
 * test before final class/item budgets are authored.</p>
 */
public final class WorldRpgMagnitudeDraft {
    public static final int LEVEL_CAP = 100;

    public static final QuadraticLevelCurve REFERENCE_HEALTH =
            new QuadraticLevelCurve(
                    LEVEL_CAP,
                    100.0,
                    6.5,
                    0.025
            );

    public static final QuadraticLevelCurve REFERENCE_PRIMARY_RESOURCE =
            new QuadraticLevelCurve(
                    LEVEL_CAP,
                    100.0,
                    3.0,
                    0.010
            );

    public static final QuadraticLevelCurve REFERENCE_BASE_POWER =
            new QuadraticLevelCurve(
                    LEVEL_CAP,
                    10.0,
                    0.45,
                    0.0015
            );

    private WorldRpgMagnitudeDraft() {
    }
}
