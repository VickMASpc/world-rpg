package dev.worldrpg.combat.condition;

import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConditionsTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/test_health");

    @Test
    void targetResourceThresholdIsPureAndExplicit() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));
        target.resources().add(HEALTH, 100.0, 1.0);

        AbilityContext context =
                new AbilityContext(source, target, 0);

        assertTrue(
                ResourceConditions.targetAtLeast(HEALTH, 1.0)
                        .evaluate(context)
                        .passed()
        );

        target.resources().require(HEALTH).drainUpTo(1.0);

        assertFalse(
                ResourceConditions.targetAtLeast(HEALTH, 0.000001)
                        .evaluate(context)
                        .passed()
        );
    }

    @Test
    void missingResourceFailsRatherThanAssumingZeroOrInfinite() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));

        AbilityContext context =
                new AbilityContext(source, target, 0);

        assertFalse(
                ResourceConditions.targetAtLeast(HEALTH, 1.0)
                        .evaluate(context)
                        .passed()
        );
    }
}
