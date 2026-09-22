package dev.worldrpg.sim;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.event.CastInterruptedEvent;
import dev.worldrpg.combat.event.CombatActorDefeatedEvent;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
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
    private long interruptions;
    private long defeats;
    private double damageApplied;
    private double healingApplied;
    private double overkill;
    private double overheal;
    private final Map<CastInterruptionReason, Long> interruptionsByReason =
            new LinkedHashMap<>();
    private final Map<CombatActorId, Long> defeatsByTarget =
            new LinkedHashMap<>();
    private final Map<ResourceKey, Double> resourceSpent =
            new LinkedHashMap<>();
    private final Map<ResourceKey, Double> resourceGained =
            new LinkedHashMap<>();
    private final Map<ResourceKey, Double> explicitRecovery =
            new LinkedHashMap<>();

    void observe(CombatEvent event) {
        if (event instanceof CombatMagnitudeResolvedEvent resolved) {
            observeMagnitude(resolved);
        }

        if (event instanceof ResourceChangedEvent resourceChanged) {
            observeResource(resourceChanged);
        }

        if (event instanceof CastInterruptedEvent interrupted) {
            interruptions++;
            interruptionsByReason.merge(
                    interrupted.reason(),
                    1L,
                    Long::sum
            );
        }

        if (event instanceof CombatActorDefeatedEvent defeated) {
            defeats++;
            defeatsByTarget.merge(
                    defeated.targetActorId(),
                    1L,
                    Long::sum
            );
        }
    }

    private void observeMagnitude(
            CombatMagnitudeResolvedEvent resolved
    ) {
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

    private void observeResource(
            ResourceChangedEvent event
    ) {
        double delta =
                event.change().after()
                        - event.change().before();

        if (delta < 0.0) {
            resourceSpent.merge(
                    event.resource(),
                    -delta,
                    Double::sum
            );
        } else if (delta > 0.0) {
            resourceGained.merge(
                    event.resource(),
                    delta,
                    Double::sum
            );
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
                interruptions,
                interruptionsByReason,
                defeats,
                defeatsByTarget,
                damageApplied,
                healingApplied,
                overkill,
                overheal,
                resourceSpent,
                resourceGained,
                explicitRecovery
        );
    }
}
