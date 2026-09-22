package dev.worldrpg.sim;

import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.math.CombatContactOutcome;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;

final class CombatSimulationMetrics {
    private long resolutions;
    private long damageResolutions;
    private long damageHits;
    private long damageMisses;
    private long healingResolutions;
    private long criticalResolutions;
    private double damageApplied;
    private double healingApplied;
    private double overkill;
    private double overheal;
    private final Map<ResourceKey, Double> explicitRecovery =
            new LinkedHashMap<>();

    void observe(CombatEvent event) {
        if (!(event instanceof CombatMagnitudeResolvedEvent resolved)) {
            return;
        }

        resolutions++;

        if (resolved.trace().critical()) {
            criticalResolutions++;
        }

        if (resolved.kind() == CombatMagnitudeKind.DAMAGE) {
            damageResolutions++;

            if (resolved.trace().contactOutcome()
                    == CombatContactOutcome.MISS) {
                damageMisses++;
            } else {
                damageHits++;
            }

            damageApplied += resolved.trace().appliedFinal();
            overkill += resolved.trace().excess();
        } else {
            healingResolutions++;
            healingApplied += resolved.trace().appliedFinal();
            overheal += resolved.trace().excess();
        }
    }

    void recordRecovery(
            ResourceKey resource,
            double applied
    ) {
        if (applied <= 0.0) {
            return;
        }

        explicitRecovery.merge(
                resource,
                applied,
                Double::sum
        );
    }

    CombatSimulationReport report(
            long startTick,
            long endTick,
            int eventCount
    ) {
        return new CombatSimulationReport(
                startTick,
                endTick,
                eventCount,
                resolutions,
                damageResolutions,
                damageHits,
                damageMisses,
                healingResolutions,
                criticalResolutions,
                damageApplied,
                healingApplied,
                overkill,
                overheal,
                explicitRecovery
        );
    }
}
