package dev.worldrpg.combat.aura;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorDirectory;
import dev.worldrpg.combat.effect.EffectContext;
import dev.worldrpg.combat.effect.EffectSequenceResult;
import dev.worldrpg.combat.event.AuraRemovedEvent;
import dev.worldrpg.combat.event.AuraTickedEvent;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Advances periodic and expiry behavior for one target's aura container.
 *
 * <p>Ticks scheduled exactly at the expiry tick execute before expiry.</p>
 */
public final class AuraRuntime {
    private final int maxCatchUpTicksPerAura;

    public AuraRuntime(int maxCatchUpTicksPerAura) {
        if (maxCatchUpTicksPerAura < 1) {
            throw new IllegalArgumentException("maxCatchUpTicksPerAura must be >= 1");
        }
        this.maxCatchUpTicksPerAura = maxCatchUpTicksPerAura;
    }

    public List<CombatEvent> advance(
            CombatActor target,
            CombatActorDirectory actors,
            long gameTick
    ) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(actors, "actors");
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }

        // Resolve all periodic sources before advancing any aura cursor.
        // A missing actor is an integration error/policy question, not a reason
        // to leave half-advanced scheduling state behind.
        for (AuraInstance instance : target.auras().instances()) {
            if (instance.definition().periodicEffect().isPresent()) {
                actors.require(instance.source());
            }
        }

        List<CombatEvent> events = new ArrayList<>();

        List<AuraPeriodicOccurrence> occurrences =
                target.auras().collectDuePeriodicTicks(
                        gameTick,
                        maxCatchUpTicksPerAura
                );

        for (AuraPeriodicOccurrence occurrence : occurrences) {
            AuraInstance instance = occurrence.instance();
            AuraPeriodicEffect periodic =
                    instance.definition().periodicEffect().orElseThrow();

            CombatActor source = actors.require(instance.source());

            events.add(new AuraTickedEvent(
                    occurrence.scheduledTick(),
                    source.id(),
                    target.id(),
                    instance.definition().id(),
                    instance.id()
            ));

            EffectSequenceResult result = periodic.effects().execute(
                    new EffectContext(
                            source,
                            target,
                            occurrence.scheduledTick()
                    )
            );

            if (result.applied()) {
                events.addAll(result.events());
            }
        }

        for (AuraRemoval removal : target.auras().expireDue(gameTick)) {
            AuraInstance instance = removal.instance();

            events.add(new AuraRemovedEvent(
                    gameTick,
                    instance.source(),
                    target.id(),
                    instance.definition().id(),
                    instance.id(),
                    removal.reason()
            ));
        }

        return List.copyOf(events);
    }
}
