package dev.worldrpg.combat.condition;

import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConditionsTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/test_health");
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/test_mana");

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

    @Test
    void targetFractionConditionUsesCurrentAgainstMaximum() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.resources().add(
                MANA,
                100.0,
                70.0
        );

        AbilityContext context =
                new AbilityContext(
                        source,
                        target,
                        0
                );

        assertTrue(
                ResourceConditions.targetAtOrBelowFraction(
                        MANA,
                        0.70
                ).evaluate(context).passed()
        );

        target.resources()
                .require(MANA)
                .setCurrent(70.0001);

        assertFalse(
                ResourceConditions.targetAtOrBelowFraction(
                        MANA,
                        0.70
                ).evaluate(context).passed()
        );
    }

    @Test
    void sourceFractionConditionFailsWhenResourceIsMissing() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        AbilityContext context =
                new AbilityContext(
                        source,
                        target,
                        0
                );

        assertFalse(
                ResourceConditions.sourceAtOrBelowFraction(
                        MANA,
                        0.50
                ).evaluate(context).passed()
        );
    }

    @Test
    void fractionConditionRejectsOutOfRangeFraction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ResourceConditions.targetAtOrBelowFraction(
                        MANA,
                        -0.01
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ResourceConditions.targetAtOrBelowFraction(
                        MANA,
                        1.01
                )
        );
    }

}
