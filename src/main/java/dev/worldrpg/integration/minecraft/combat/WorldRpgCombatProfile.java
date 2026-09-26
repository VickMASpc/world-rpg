package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.WorldRpgDefenseDraft;
import dev.worldrpg.combat.resource.ResourceKey;

public final class WorldRpgCombatProfile {
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    public static final ResourceKey FOCUS =
            ResourceKey.of("world_rpg:resource/focus");

    public static final double PLAYER_MAX_HEALTH = 100.0;
    public static final double PLAYER_MAX_FOCUS = 100.0;
    public static final double PLAYER_ATTACK_POWER = 14.0;

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
}
