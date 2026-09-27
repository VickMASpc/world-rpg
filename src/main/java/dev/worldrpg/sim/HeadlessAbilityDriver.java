package dev.worldrpg.sim;

import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.ability.AbilityObservationProvider;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.AbilityActivationResult;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.event.CastInterruptedEvent;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Headless adapter over the production P3 CastController.
 *
 * <p>The simulator owns the clock/journal. Cast lifecycle, costs, cooldowns,
 * channel cadence and effect execution remain production P3 behavior.</p>
 */
public final class HeadlessAbilityDriver {
    private final HeadlessCombatSimulator simulator;
    private final CastController casts;

    public HeadlessAbilityDriver(
            HeadlessCombatSimulator simulator,
            CombatActor owner
    ) {
        this(
                simulator,
                owner,
                new CooldownBook(),
                AbilityObservationProvider.unavailable()
        );
    }

    public HeadlessAbilityDriver(
            HeadlessCombatSimulator simulator,
            CombatActor owner,
            CooldownBook cooldowns,
            AbilityObservationProvider observations
    ) {
        this.simulator =
                Objects.requireNonNull(simulator, "simulator");
        this.casts = new CastController(
                Objects.requireNonNull(owner, "owner"),
                Objects.requireNonNull(cooldowns, "cooldowns"),
                Objects.requireNonNull(observations, "observations")
        );
    }

    public CastController casts() {
        return casts;
    }

    public AbilityActivationResult activate(
            AbilityDefinition ability,
            CombatActor target
    ) {
        AbilityActivationResult result =
                casts.tryActivate(
                        Objects.requireNonNull(ability, "ability"),
                        Objects.requireNonNull(target, "target"),
                        simulator.now()
                );

        simulator.record(result.events());
        return result;
    }

    /**
     * Advances the shared simulation clock, then lets the production cast
     * controller catch up to that tick.
     */
    public List<CombatEvent> advanceAndTick(long ticks) {
        simulator.advance(ticks);

        List<CombatEvent> events =
                casts.tick(simulator.now());

        simulator.record(events);
        return events;
    }

    /**
     * Processes cast state at the current simulation tick without advancing.
     */
    public List<CombatEvent> tickNow() {
        List<CombatEvent> events =
                casts.tick(simulator.now());

        simulator.record(events);
        return events;
    }

    public Optional<CastInterruptedEvent> interrupt(
            CastInterruptionReason reason
    ) {
        Optional<CastInterruptedEvent> event =
                casts.interrupt(
                        Objects.requireNonNull(reason, "reason"),
                        simulator.now()
                );

        event.ifPresent(value ->
                simulator.record(List.of(value))
        );

        return event;
    }
}
