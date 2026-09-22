package dev.worldrpg.combat.aura;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorDirectory;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.event.AuraRemovedEvent;
import dev.worldrpg.combat.event.AuraTickedEvent;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuraRuntimeTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/test_periodic_health");

    @Test
    void delayedAdvanceCatchesUpTicksInScheduleOrderThenExpires() {
        CombatActor source = actor(1, 100);
        CombatActor target = actor(2, 100);

        CombatActorDirectory directory = new CombatActorDirectory();
        directory.register(source);
        directory.register(target);

        AuraDefinition aura = periodicAura(
                "world_rpg:aura/test_dot",
                12,
                4,
                AuraTickRefreshPolicy.KEEP_SCHEDULE
        );

        target.auras().apply(aura, source.id(), 100);

        AuraRuntime runtime = new AuraRuntime(8);
        var events = runtime.advance(target, directory, 112);

        assertEquals(85.0, target.resources().require(HEALTH).current());
        assertTrue(target.auras().instances().isEmpty());

        assertEquals(7, events.size());
        assertTrue(events.get(0) instanceof AuraTickedEvent);
        assertEquals(104, events.get(0).gameTick());
        assertTrue(events.get(2) instanceof AuraTickedEvent);
        assertEquals(108, events.get(2).gameTick());
        assertTrue(events.get(4) instanceof AuraTickedEvent);
        assertEquals(112, events.get(4).gameTick());
        assertTrue(events.get(6) instanceof AuraRemovedEvent);
    }

    @Test
    void keepScheduleDoesNotPushNextTickWhenAuraRefreshes() {
        CombatActor source = actor(1, 100);
        CombatActor target = actor(2, 100);

        AuraDefinition aura = periodicAura(
                "world_rpg:aura/test_keep_schedule",
                20,
                5,
                AuraTickRefreshPolicy.KEEP_SCHEDULE
        );

        AuraInstance first = target.auras().apply(aura, source.id(), 0).instance();
        assertEquals(5, first.nextPeriodicTick().orElseThrow());

        AuraInstance refreshed = target.auras().apply(aura, source.id(), 3).instance();
        assertEquals(5, refreshed.nextPeriodicTick().orElseThrow());
    }

    @Test
    void resetScheduleMovesNextTickOnRefresh() {
        CombatActor source = actor(1, 100);
        CombatActor target = actor(2, 100);

        AuraDefinition aura = periodicAura(
                "world_rpg:aura/test_reset_schedule",
                20,
                5,
                AuraTickRefreshPolicy.RESET_SCHEDULE
        );

        AuraInstance first = target.auras().apply(aura, source.id(), 0).instance();
        assertEquals(5, first.nextPeriodicTick().orElseThrow());

        AuraInstance refreshed = target.auras().apply(aura, source.id(), 3).instance();
        assertEquals(8, refreshed.nextPeriodicTick().orElseThrow());
    }

    @Test
    void catchUpLimitRejectsPathologicalTimeJumpBeforeAdvancingCursor() {
        CombatActor source = actor(1, 100);
        CombatActor target = actor(2, 100);

        AuraDefinition aura = new AuraDefinition(
                RpgId.parse("world_rpg:aura/test_unbounded"),
                1,
                OptionalLong.empty(),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.KEEP_EXISTING,
                List.of(),
                Optional.of(new AuraPeriodicEffect(
                        1,
                        AuraTickRefreshPolicy.KEEP_SCHEDULE,
                        new EffectSequence(List.of())
                ))
        );

        AuraInstance instance = target.auras().apply(aura, source.id(), 0).instance();

        assertThrows(
                AuraPeriodicLimitException.class,
                () -> target.auras().collectDuePeriodicTicks(100, 8)
        );

        assertEquals(1, instance.nextPeriodicTick().orElseThrow());
    }

    private static CombatActor actor(long id, double health) {
        CombatActor actor = new CombatActor(new CombatActorId(id));
        actor.resources().add(HEALTH, health, health);
        return actor;
    }

    private static AuraDefinition periodicAura(
            String id,
            long duration,
            long interval,
            AuraTickRefreshPolicy tickRefresh
    ) {
        return new AuraDefinition(
                RpgId.parse(id),
                1,
                OptionalLong.of(duration),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.RESET_DURATION,
                List.of(),
                Optional.of(new AuraPeriodicEffect(
                        interval,
                        tickRefresh,
                        new EffectSequence(List.of(
                                new ResourceDrainEffect(
                                        EffectRecipient.TARGET,
                                        HEALTH,
                                        5.0
                                )
                        ))
                ))
        );
    }
}
