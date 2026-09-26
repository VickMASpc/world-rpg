package dev.worldrpg.combat.resolution;

import dev.worldrpg.api.id.RpgId;

/**
 * Stable semantic identities for baseline magnitude-resolution behavior.
 *
 * <p>P3 carries the identity but does not interpret its mathematics. P4 maps
 * these IDs to calibrated outcome profiles.</p>
 */
public final class CombatResolutionProfileIds {
    public static final RpgId GUARANTEED =
            RpgId.parse("world_rpg:resolution/guaranteed");
    public static final RpgId DIRECT_WEAPON =
            RpgId.parse("world_rpg:resolution/direct_weapon");
    public static final RpgId DIRECT_SPELL =
            RpgId.parse("world_rpg:resolution/direct_spell");
    public static final RpgId PERIODIC =
            RpgId.parse("world_rpg:resolution/periodic");

    private CombatResolutionProfileIds() {
    }
}
