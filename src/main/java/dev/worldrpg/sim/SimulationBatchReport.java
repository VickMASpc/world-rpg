package dev.worldrpg.sim;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.resource.ResourceKey;

import java.util.List;
import java.util.Objects;

/**
 * Immutable collection of deterministic seeded scenario results with common
 * aggregate metrics.
 */
public record SimulationBatchReport(
        List<SeededSimulationResult> runs
) {
    public SimulationBatchReport {
        runs = List.copyOf(
                Objects.requireNonNull(runs, "runs")
        );

        if (runs.isEmpty()) {
            throw new IllegalArgumentException(
                    "simulation batch must contain at least one run"
            );
        }
    }

    public int runCount() {
        return runs.size();
    }

    public double averageElapsedTicks() {
        return runs.stream()
                .mapToLong(result ->
                        result.report().elapsedTicks()
                )
                .average()
                .orElseThrow();
    }

    public long minimumElapsedTicks() {
        return runs.stream()
                .mapToLong(result ->
                        result.report().elapsedTicks()
                )
                .min()
                .orElseThrow();
    }

    public long maximumElapsedTicks() {
        return runs.stream()
                .mapToLong(result ->
                        result.report().elapsedTicks()
                )
                .max()
                .orElseThrow();
    }

    public double averageDamageApplied() {
        return runs.stream()
                .mapToDouble(result ->
                        result.report().damageApplied()
                )
                .average()
                .orElseThrow();
    }

    public double averageHealingApplied() {
        return runs.stream()
                .mapToDouble(result ->
                        result.report().healingApplied()
                )
                .average()
                .orElseThrow();
    }

    public double damageMissRate() {
        long attempts = runs.stream()
                .mapToLong(result ->
                        result.report().damageResolutions()
                )
                .sum();

        if (attempts == 0L) {
            return 0.0;
        }

        long misses = runs.stream()
                .mapToLong(result ->
                        result.report().damageMisses()
                )
                .sum();

        return (double) misses / (double) attempts;
    }

    public double defeatRateOf(
            CombatActorId actorId
    ) {
        Objects.requireNonNull(actorId, "actorId");

        long defeatedRuns = runs.stream()
                .filter(result ->
                        result.report().defeatsOf(actorId) > 0L
                )
                .count();

        return (double) defeatedRuns
                / (double) runs.size();
    }

    public double averageDefeatsOf(
            CombatActorId actorId
    ) {
        Objects.requireNonNull(actorId, "actorId");

        return runs.stream()
                .mapToLong(result ->
                        result.report().defeatsOf(actorId)
                )
                .average()
                .orElseThrow();
    }

    public double averageDefeats() {
        return runs.stream()
                .mapToLong(result ->
                        result.report().defeats()
                )
                .average()
                .orElseThrow();
    }

    public double averageResourceSpent(
            ResourceKey resource
    ) {
        Objects.requireNonNull(resource, "resource");

        return runs.stream()
                .mapToDouble(result ->
                        result.report().spent(resource)
                )
                .average()
                .orElseThrow();
    }

    public double averageResourceGained(
            ResourceKey resource
    ) {
        Objects.requireNonNull(resource, "resource");

        return runs.stream()
                .mapToDouble(result ->
                        result.report().gained(resource)
                )
                .average()
                .orElseThrow();
    }

    public double averageCriticalResolutions() {
        return runs.stream()
                .mapToLong(result ->
                        result.report().criticalResolutions()
                )
                .average()
                .orElseThrow();
    }

    public double averageExplicitRecovery(
            ResourceKey resource
    ) {
        Objects.requireNonNull(resource, "resource");

        return runs.stream()
                .mapToDouble(result ->
                        result.report()
                                .explicitRecovery()
                                .getOrDefault(resource, 0.0)
                )
                .average()
                .orElseThrow();
    }
}
