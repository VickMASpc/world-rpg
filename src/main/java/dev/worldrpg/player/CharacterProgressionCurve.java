package dev.worldrpg.player;

public final class CharacterProgressionCurve {
    public static final int MAX_LEVEL = 100;

    private CharacterProgressionCurve() {
    }

    public static long experienceToNextLevel(int level) {
        if (level < 1 || level >= MAX_LEVEL) {
            if (level == MAX_LEVEL) {
                return 0L;
            }
            throw new IllegalArgumentException(
                    "level must be between 1 and " + MAX_LEVEL
            );
        }

        long linear = 300L + (long) (level - 1) * 180L;
        long growth = Math.round(
                Math.pow(level - 1, 1.35) * 30.0
        );
        return Math.addExact(linear, growth);
    }
}
