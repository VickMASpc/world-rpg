package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatActorDefeatedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProfiledCombatResolverDefeatTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    @Test
    void defeatEmitsOnlyOnPositiveToZeroTransition() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));
        target.resources().add(
                HEALTH,
                10.0,
                10.0
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        new CombatMathProfile(
                                HEALTH,
                                0.0,
                                0.0,
                                0.0,
                                2.0,
                                1.0,
                                100.0,
                                0.75
                        ),
                        () -> 0.5
                );

        var first = resolver.resolve(
                request(source, target, 0)
        );

        assertEquals(3, first.size());
        assertInstanceOf(
                CombatActorDefeatedEvent.class,
                first.get(2)
        );
        assertEquals(
                0.0,
                target.resources().require(HEALTH).current(),
                0.0
        );

        var second = resolver.resolve(
                request(source, target, 1)
        );

        assertEquals(2, second.size());
        assertEquals(
                0.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    private static CombatMagnitudeRequest request(
            CombatActor source,
            CombatActor target,
            long tick
    ) {
        return new CombatMagnitudeRequest(
                tick,
                CombatMagnitudeKind.DAMAGE,
                source,
                target,
                RpgId.parse("world_rpg:ability/test/defeat"),
                CombatSchools.PHYSICAL.id(),
                20.0
        );
    }
}
