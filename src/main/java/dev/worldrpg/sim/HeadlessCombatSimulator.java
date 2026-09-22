package dev.worldrpg.sim;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.resource.ResourcePool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Small deterministic simulator shell around the production resolution
 * gateway.
 *
 * <p>Actor objects persist until the caller explicitly replaces them. Advancing
 * time performs no implicit recovery or encounter reset.</p>
 */
public final class HeadlessCombatSimulator {
    private final CombatResolutionGateway resolver;
    private final SimulationClock clock;
    private final long startTick;
    private final List<CombatEvent> events = new ArrayList<>();
    private final CombatSimulationMetrics metrics =
            new CombatSimulationMetrics();

    public HeadlessCombatSimulator(
            CombatResolutionGateway resolver
    ) {
        this(resolver, new SimulationClock());
    }

    public HeadlessCombatSimulator(
            CombatResolutionGateway resolver,
            SimulationClock clock
    ) {
        this.resolver =
                Objects.requireNonNull(resolver, "resolver");
        this.clock =
                Objects.requireNonNull(clock, "clock");
        startTick = clock.now();
    }

    public long now() {
        return clock.now();
    }

    public long advance(long ticks) {
        return clock.advance(ticks);
    }

    public List<CombatEvent> resolve(
            CombatMagnitudeKind kind,
            CombatActor source,
            CombatActor target,
            RpgId causeId,
            RpgId schoolId,
            double authoredBaseMagnitude
    ) {
        CombatMagnitudeRequest request =
                new CombatMagnitudeRequest(
                        clock.now(),
                        kind,
                        Objects.requireNonNull(source, "source"),
                        Objects.requireNonNull(target, "target"),
                        Objects.requireNonNull(causeId, "causeId"),
                        Objects.requireNonNull(schoolId, "schoolId"),
                        authoredBaseMagnitude
                );

        List<CombatEvent> resolved =
                List.copyOf(resolver.resolve(request));

        record(resolved);
        return resolved;
    }

    /**
     * Explicit out-of-combat recovery action.
     *
     * <p>This is deliberately an action, not a side effect of elapsed time.</p>
     */
    public ResourceChangedEvent recover(
            CombatActor actor,
            ResourceKey resource,
            double amount
    ) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(resource, "resource");

        ResourcePool pool = actor.resources().require(resource);
        ResourceChange change = pool.gain(amount);

        ResourceChangedEvent event =
                new ResourceChangedEvent(
                        clock.now(),
                        actor.id(),
                        resource,
                        change
                );

        events.add(event);
        metrics.recordRecovery(resource, change.applied());
        return event;
    }

    /**
     * Allows later cast/ability simulation to feed generic kernel events into
     * the same journal/metrics without duplicating resolution formulas.
     */
    public void record(
            List<? extends CombatEvent> newEvents
    ) {
        Objects.requireNonNull(newEvents, "newEvents");

        for (CombatEvent event : newEvents) {
            CombatEvent checked =
                    Objects.requireNonNull(event, "event");
            events.add(checked);
            metrics.observe(checked);
        }
    }

    public List<CombatEvent> events() {
        return List.copyOf(events);
    }

    public CombatSimulationReport report() {
        return metrics.report(
                startTick,
                clock.now(),
                events.size()
        );
    }
}
