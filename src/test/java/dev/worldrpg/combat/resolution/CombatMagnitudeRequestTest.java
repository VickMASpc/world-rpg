package dev.worldrpg.combat.resolution;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombatMagnitudeRequestTest {
    private final CombatActor source =
            new CombatActor(new CombatActorId(1));
    private final CombatActor target =
            new CombatActor(new CombatActorId(2));

    @Test
    void requestCarriesInputsWithoutResolvingMath() {
        CombatMagnitudeRequest request =
                new CombatMagnitudeRequest(
                        20,
                        CombatMagnitudeKind.DAMAGE,
                        source,
                        target,
                        RpgId.parse("world_rpg:ability/test/bolt"),
                        RpgId.parse("world_rpg:school/arcane"),
                        12.5
                );

        assertEquals(12.5, request.authoredBaseMagnitude());
        assertEquals(CombatMagnitudeKind.DAMAGE, request.kind());
        assertEquals(source, request.source());
        assertEquals(target, request.target());
    }

    @Test
    void invalidMagnitudeAndTickAreRejectedAtBoundary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> request(-1, 1.0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> request(0, -0.01)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> request(0, Double.NaN)
        );
    }

    private CombatMagnitudeRequest request(
            long gameTick,
            double magnitude
    ) {
        return new CombatMagnitudeRequest(
                gameTick,
                CombatMagnitudeKind.HEALING,
                source,
                target,
                RpgId.parse("world_rpg:ability/test/heal"),
                RpgId.parse("world_rpg:school/holy"),
                magnitude
        );
    }
}
