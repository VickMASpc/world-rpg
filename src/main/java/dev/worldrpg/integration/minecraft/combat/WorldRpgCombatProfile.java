package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.WorldRpgDefenseDraft;
import dev.worldrpg.combat.resource.ResourceKey;

public final class WorldRpgCombatProfile {
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey FOCUS =
            ResourceKey.of("world_rpg:resource/focus");

    public static final double PLAYER_BASE_HEALTH = 100.0;
    public static final double PLAYER_HEALTH_PER_LEVEL = 12.0;
    public static final double PLAYER_MAX_FOCUS = 100.0;
    public static final double PLAYER_BASE_ATTACK_POWER = 14.0;
    public static final double PLAYER_ATTACK_POWER_PER_LEVEL = 2.0;

    // Compatibility aliases for older proof code.
    public static final double PLAYER_MAX_HEALTH =
            PLAYER_BASE_HEALTH;
    public static final double PLAYER_ATTACK_POWER =
            PLAYER_BASE_ATTACK_POWER;

    public static final CombatMathProfile MATH =
            new CombatMathProfile(
                    HEALTH,
                    0.35,
                    0.45,
                    0.45,
                    1.5,
                    0.50,
                    WorldRpgDefenseDraft.MITIGATION_SCALE,
                    0.75
            );

    private WorldRpgCombatProfile() {
    }

    public static double playerMaximumHealth(int level) {
        requireLevel(level);
        return PLAYER_BASE_HEALTH
                + (level - 1) * PLAYER_HEALTH_PER_LEVEL;
    }

    public static double playerAttackPower(int level) {
        requireLevel(level);
        return PLAYER_BASE_ATTACK_POWER
                + (level - 1) * PLAYER_ATTACK_POWER_PER_LEVEL;
    }

    private static void requireLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException(
                    "level must be >= 1"
            );
        }
    }
}
