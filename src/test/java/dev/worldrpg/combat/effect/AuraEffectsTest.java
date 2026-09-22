package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.aura.AuraRefreshPolicy;
import dev.worldrpg.combat.aura.AuraRemovalReason;
import dev.worldrpg.combat.aura.AuraStatModifier;
import dev.worldrpg.combat.aura.AuraUniqueness;
import dev.worldrpg.combat.event.AuraAppliedEvent;
import dev.worldrpg.combat.event.AuraRemovedEvent;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuraEffectsTest {
    private static final StatKey POWER =
            StatKey.of("world_rpg:stat/test_power");

    @Test
    void auraCanBeAppliedAndRemovedThroughGenericEffects() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));
        target.stats().setBase(POWER, 100.0);

        AuraDefinition aura = new AuraDefinition(
                RpgId.parse("world_rpg:aura/test_effect"),
                1,
                OptionalLong.of(40),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.RESET_DURATION,
                List.of(new AuraStatModifier(
                        POWER,
                        StatModifierOperation.ADD,
                        25.0,
                        0
                ))
        );

        EffectSequence apply = new EffectSequence(List.of(
                new ApplyAuraEffect(EffectRecipient.TARGET, aura)
        ));

        EffectSequenceResult applied = apply.execute(
                new EffectContext(source, target, 10)
        );

        assertTrue(applied.applied());
        assertTrue(applied.events().get(0) instanceof AuraAppliedEvent);
        assertEquals(125.0, target.stats().value(POWER));

        EffectSequence remove = new EffectSequence(List.of(
                new RemoveAuraEffect(
                        EffectRecipient.TARGET,
                        aura.id(),
                        AuraRemovalReason.DISPELLED
                )
        ));

        EffectSequenceResult removed = remove.execute(
                new EffectContext(source, target, 20)
        );

        assertTrue(removed.events().get(0) instanceof AuraRemovedEvent);
        assertEquals(100.0, target.stats().value(POWER));
        assertTrue(target.auras().instances().isEmpty());
    }
}
