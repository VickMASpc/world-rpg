package dev.worldrpg.content.combat;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.aura.AuraRefreshPolicy;
import dev.worldrpg.combat.aura.AuraStatModifier;
import dev.worldrpg.combat.aura.AuraUniqueness;

import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

public record AuraContentDefinition(
        RpgId id,
        int maxStacks,
        OptionalLong durationTicks,
        AuraUniqueness uniqueness,
        AuraRefreshPolicy refreshPolicy,
        List<AuraStatModifier> statModifiers
) implements RpgDefinition {
    public AuraContentDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(durationTicks, "durationTicks");
        Objects.requireNonNull(uniqueness, "uniqueness");
        Objects.requireNonNull(refreshPolicy, "refreshPolicy");
        statModifiers = List.copyOf(
                Objects.requireNonNull(statModifiers, "statModifiers")
        );

        new AuraDefinition(
                id,
                maxStacks,
                durationTicks,
                uniqueness,
                refreshPolicy,
                statModifiers
        );
    }

    public AuraDefinition compile() {
        return new AuraDefinition(
                id,
                maxStacks,
                durationTicks,
                uniqueness,
                refreshPolicy,
                statModifiers
        );
    }
}
