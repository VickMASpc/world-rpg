package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Immutable P4 lookup from stable resolution-profile IDs to math semantics.
 */
public final class CombatOutcomeProfileSet {
    private final Map<RpgId, CombatOutcomeProfile> profiles;

    public CombatOutcomeProfileSet(
            Map<RpgId, CombatOutcomeProfile> profiles
    ) {
        Objects.requireNonNull(profiles, "profiles");

        Map<RpgId, CombatOutcomeProfile> copy =
                new LinkedHashMap<>();

        for (Map.Entry<RpgId, CombatOutcomeProfile> entry :
                profiles.entrySet()) {
            RpgId id = Objects.requireNonNull(
                    entry.getKey(),
                    "profile id"
            );
            CombatOutcomeProfile profile = Objects.requireNonNull(
                    entry.getValue(),
                    "profile"
            );

            if (!id.equals(profile.id())) {
                throw new IllegalArgumentException(
                        "profile map key does not match profile ID: " + id
                );
            }

            copy.put(id, profile);
        }

        this.profiles =
                java.util.Collections.unmodifiableMap(copy);
    }

    public static CombatOutcomeProfileSet guaranteedOnly() {
        CombatOutcomeProfile guaranteed =
                new CombatOutcomeProfile(
                        CombatResolutionProfileIds.GUARANTEED,
                        0.0,
                        true
                );

        return new CombatOutcomeProfileSet(
                Map.of(guaranteed.id(), guaranteed)
        );
    }

    public java.util.Optional<CombatOutcomeProfile> find(RpgId id) {
        return java.util.Optional.ofNullable(
                profiles.get(
                        Objects.requireNonNull(id, "id")
                )
        );
    }

    public CombatOutcomeProfile require(RpgId id) {
        CombatOutcomeProfile profile =
                profiles.get(Objects.requireNonNull(id, "id"));

        if (profile == null) {
            throw new NoSuchElementException(
                    "Unknown combat resolution profile: " + id
            );
        }

        return profile;
    }

    public Map<RpgId, CombatOutcomeProfile> asMap() {
        return profiles;
    }
}
