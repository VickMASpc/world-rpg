package dev.worldrpg.progression;

/**
 * Expected total-playtime pacing target independent of XP currency.
 *
 * <p>The profile describes how long the transition from level L to L+1 should
 * take in a representative blind first-character journey. It does not decide
 * how much XP exists or where that XP comes from.</p>
 */
public record ProgressionPaceProfile(
        int levelCap,
        double firstTransitionHours,
        double finalTransitionHours,
        double growthExponent
) {
    public ProgressionPaceProfile {
        if (levelCap < 2) {
            throw new IllegalArgumentException(
                    "levelCap must be >= 2"
            );
        }
        requirePositiveFinite(
                firstTransitionHours,
                "firstTransitionHours"
        );
        requirePositiveFinite(
                finalTransitionHours,
                "finalTransitionHours"
        );
        requirePositiveFinite(
                growthExponent,
                "growthExponent"
        );

        if (finalTransitionHours < firstTransitionHours) {
            throw new IllegalArgumentException(
                    "finalTransitionHours must be >= firstTransitionHours"
            );
        }
    }

    /**
     * Expected hours spent advancing from {@code level} to {@code level + 1}.
     */
    public double hoursFromLevel(int level) {
        requireTransitionLevel(level);

        if (levelCap == 2) {
            return firstTransitionHours;
        }

        double normalized =
                (double) (level - 1)
                        / (double) (levelCap - 2);

        return firstTransitionHours
                + (finalTransitionHours - firstTransitionHours)
                * Math.pow(normalized, growthExponent);
    }

    /**
     * Expected cumulative playtime needed to arrive at the given level from
     * level 1.
     */
    public double cumulativeHoursToReach(int level) {
        requireCharacterLevel(level);

        double total = 0.0;
        for (int current = 1; current < level; current++) {
            total += hoursFromLevel(current);
        }
        return total;
    }

    public double journeyHours() {
        return cumulativeHoursToReach(levelCap);
    }

    private void requireTransitionLevel(int level) {
        if (level < 1 || level >= levelCap) {
            throw new IllegalArgumentException(
                    "transition level must be between 1 and "
                            + (levelCap - 1)
            );
        }
    }

    private void requireCharacterLevel(int level) {
        if (level < 1 || level > levelCap) {
            throw new IllegalArgumentException(
                    "character level must be between 1 and "
                            + levelCap
            );
        }
    }

    private static void requirePositiveFinite(
            double value,
            String label
    ) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(
                    label + " must be finite and > 0"
            );
        }
    }
}
