package dev.worldrpg.combat.math;

import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * First P4 contact/critical calibration set.
 *
 * <p>Profile identities are stable. Numerical miss chances remain calibration
 * data until scenario batches support them.</p>
 */
public final class WorldRpgOutcomeDraft {
    private WorldRpgOutcomeDraft() {
    }

    public static CombatOutcomeProfileSet referenceProfiles() {
        Map<dev.worldrpg.api.id.RpgId, CombatOutcomeProfile> profiles =
                new LinkedHashMap<>();

        add(
                profiles,
                new CombatOutcomeProfile(
                        CombatResolutionProfileIds.GUARANTEED,
                        0.0,
                        true
                )
        );
        add(
                profiles,
                new CombatOutcomeProfile(
                        CombatResolutionProfileIds.DIRECT_WEAPON,
                        0.05,
                        true
                )
        );
        add(
                profiles,
                new CombatOutcomeProfile(
                        CombatResolutionProfileIds.DIRECT_SPELL,
                        0.04,
                        true
                )
        );
        add(
                profiles,
                new CombatOutcomeProfile(
                        CombatResolutionProfileIds.PERIODIC,
                        0.0,
                        false
                )
        );

        return new CombatOutcomeProfileSet(profiles);
    }

    private static void add(
            Map<dev.worldrpg.api.id.RpgId, CombatOutcomeProfile> profiles,
            CombatOutcomeProfile profile
    ) {
        profiles.put(profile.id(), profile);
    }
}
