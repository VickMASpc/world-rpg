package dev.worldrpg.combat.aura;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuraContainerTest {
    private static final StatKey POWER =
            StatKey.of("world_rpg:stat/test_power");

    @Test
    void perTargetAuraStacksAndScalesStatModifier() {
        CombatActor target = actor(2);
        target.stats().setBase(POWER, 100.0);

        AuraDefinition aura = definition(
                "world_rpg:aura/test_power",
                3,
                OptionalLong.of(20),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.RESET_DURATION,
                List.of(new AuraStatModifier(
                        POWER,
                        StatModifierOperation.ADD,
                        10.0,
                        0
                ))
        );

        AuraApplicationResult first =
                target.auras().apply(aura, new CombatActorId(1), 5);
        AuraApplicationResult second =
                target.auras().apply(aura, new CombatActorId(3), 8);

        assertTrue(first.created());
        assertFalse(second.created());
        assertEquals(first.instance().id(), second.instance().id());
        assertEquals(2, second.currentStacks());
        assertEquals(120.0, target.stats().value(POWER));
        assertEquals(28, second.instance().expiresAtTick().orElseThrow());
    }

    @Test
    void perSourceAllowsSameDefinitionFromDifferentActors() {
        CombatActor target = actor(3);

        AuraDefinition aura = definition(
                "world_rpg:aura/test_per_source",
                1,
                OptionalLong.empty(),
                AuraUniqueness.PER_SOURCE,
                AuraRefreshPolicy.KEEP_EXISTING,
                List.of()
        );

        target.auras().apply(aura, new CombatActorId(1), 0);
        target.auras().apply(aura, new CombatActorId(2), 0);

        assertEquals(2, target.auras().instances().size());
    }

    @Test
    void expiryRemovesAuraAndItsStatModifiers() {
        CombatActor target = actor(4);
        target.stats().setBase(POWER, 50.0);

        AuraDefinition aura = definition(
                "world_rpg:aura/test_expiry",
                1,
                OptionalLong.of(10),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.RESET_DURATION,
                List.of(new AuraStatModifier(
                        POWER,
                        StatModifierOperation.MULTIPLY,
                        2.0,
                        0
                ))
        );

        AuraApplicationResult applied =
                target.auras().apply(aura, new CombatActorId(1), 20);

        assertEquals(100.0, target.stats().value(POWER));
        assertTrue(target.auras().expireDue(29).isEmpty());

        List<AuraRemoval> removals = target.auras().expireDue(30);

        assertEquals(1, removals.size());
        assertEquals(AuraRemovalReason.EXPIRED, removals.get(0).reason());
        assertFalse(target.auras().find(applied.instance().id()).isPresent());
        assertEquals(50.0, target.stats().value(POWER));
    }

    private static CombatActor actor(long id) {
        return new CombatActor(new CombatActorId(id));
    }

    private static AuraDefinition definition(
            String id,
            int maxStacks,
            OptionalLong duration,
            AuraUniqueness uniqueness,
            AuraRefreshPolicy refresh,
            List<AuraStatModifier> modifiers
    ) {
        return new AuraDefinition(
                RpgId.parse(id),
                maxStacks,
                duration,
                uniqueness,
                refresh,
                modifiers
        );
    }
}
