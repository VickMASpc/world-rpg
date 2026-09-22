package dev.worldrpg.combat.aura;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public record AuraDefinition(
        RpgId id,
        int maxStacks,
        OptionalLong durationTicks,
        AuraUniqueness uniqueness,
        AuraRefreshPolicy refreshPolicy,
        List<AuraStatModifier> statModifiers,
        Optional<AuraPeriodicEffect> periodicEffect
) implements RpgDefinition {
    public AuraDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(durationTicks, "durationTicks");
        Objects.requireNonNull(uniqueness, "uniqueness");
        Objects.requireNonNull(refreshPolicy, "refreshPolicy");
        statModifiers = List.copyOf(Objects.requireNonNull(statModifiers, "statModifiers"));
        Objects.requireNonNull(periodicEffect, "periodicEffect");

        if (maxStacks < 1) {
            throw new IllegalArgumentException("maxStacks must be >= 1");
        }
        if (durationTicks.isPresent() && durationTicks.getAsLong() < 0) {
            throw new IllegalArgumentException("durationTicks must be >= 0 when present");
        }
    }

    public AuraDefinition(
            RpgId id,
            int maxStacks,
            OptionalLong durationTicks,
            AuraUniqueness uniqueness,
            AuraRefreshPolicy refreshPolicy,
            List<AuraStatModifier> statModifiers
    ) {
        this(
                id,
                maxStacks,
                durationTicks,
                uniqueness,
                refreshPolicy,
                statModifiers,
                Optional.empty()
        );
    }
}
