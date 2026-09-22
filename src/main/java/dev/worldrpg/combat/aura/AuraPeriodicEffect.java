package dev.worldrpg.combat.aura;

import dev.worldrpg.combat.effect.EffectSequence;

import java.util.Objects;

public record AuraPeriodicEffect(
        long intervalTicks,
        AuraTickRefreshPolicy refreshPolicy,
        EffectSequence effects
) {
    public AuraPeriodicEffect {
        if (intervalTicks < 1) {
            throw new IllegalArgumentException("periodic aura interval must be >= 1 tick");
        }
        Objects.requireNonNull(refreshPolicy, "refreshPolicy");
        Objects.requireNonNull(effects, "effects");
    }
}
