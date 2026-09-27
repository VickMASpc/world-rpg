package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.absorb.CombatAbsorbGateway;
import dev.worldrpg.combat.absorb.CombatAbsorbResult;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatAbsorbedEvent;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProfiledCombatResolverAbsorbTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    @Test
    void partialAbsorbRunsAfterMitigationAndBeforeHealth() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.resources().add(HEALTH, 100.0, 100.0);
        target.stats().setBase(
                CombatMathStats.ARMOR,
                100.0
        );

        CombatAbsorbGateway absorbs =
                (request, incoming) -> {
                    assertEquals(30.0, incoming, 1.0e-12);
                    return new CombatAbsorbResult(
                            incoming,
                            10.0,
                            incoming - 10.0
                    );
                };

        ProfiledCombatResolver resolver =
                resolver(absorbs);

        var events = resolver.resolve(
                damage(source, target, 60.0)
        );

        CombatMagnitudeResolvedEvent resolved =
                (CombatMagnitudeResolvedEvent) events.get(0);

        assertEquals(
                30.0,
                resolved.trace().beforeAbsorb(),
                1.0e-12
        );
        assertEquals(
                10.0,
                resolved.trace().absorbed(),
                0.0
        );
        assertEquals(
                20.0,
                resolved.trace().requestedFinal(),
                1.0e-12
        );
        assertEquals(
                80.0,
                target.resources().require(HEALTH).current(),
                1.0e-12
        );
        assertInstanceOf(
                CombatAbsorbedEvent.class,
                events.get(1)
        );
    }

    @Test
    void fullAbsorbPreventsHealthLoss() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.resources().add(HEALTH, 100.0, 100.0);

        ProfiledCombatResolver resolver =
                resolver(
                        (request, incoming) ->
                                new CombatAbsorbResult(
                                        incoming,
                                        incoming,
                                        0.0
                                )
                );

        CombatMagnitudeResolvedEvent resolved =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        damage(source, target, 25.0)
                ).get(0);

        assertEquals(25.0, resolved.trace().absorbed(), 0.0);
        assertEquals(0.0, resolved.trace().requestedFinal(), 0.0);
        assertEquals(0.0, resolved.trace().appliedFinal(), 0.0);
        assertEquals(
                100.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    @Test
    void healingNeverConsultsDamageAbsorbGateway() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.resources().add(HEALTH, 100.0, 50.0);

        AtomicInteger calls = new AtomicInteger();

        CombatAbsorbGateway absorbs =
                (request, incoming) -> {
                    calls.incrementAndGet();
                    throw new AssertionError(
                            "healing must not enter damage absorb"
                    );
                };

        ProfiledCombatResolver resolver =
                resolver(absorbs);

        resolver.resolve(
                new CombatMagnitudeRequest(
                        0,
                        CombatMagnitudeKind.HEALING,
                        source,
                        target,
                        RpgId.parse(
                                "world_rpg:ability/test/heal_absorb_boundary"
                        ),
                        CombatSchools.HOLY.id(),
                        25.0
                )
        );

        assertEquals(0, calls.get());
        assertEquals(
                75.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    private static ProfiledCombatResolver resolver(
            CombatAbsorbGateway absorbs
    ) {
        return new ProfiledCombatResolver(
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
                () -> 0.5,
                CombatStatResolver.direct(),
                CombatLevelSource.constant(1),
                CombatOutcomeProfileSet.guaranteedOnly(),
                absorbs
        );
    }

    private static CombatMagnitudeRequest damage(
            CombatActor source,
            CombatActor target,
            double magnitude
    ) {
        return new CombatMagnitudeRequest(
                0,
                CombatMagnitudeKind.DAMAGE,
                source,
                target,
                RpgId.parse(
                        "world_rpg:ability/test/absorb"
                ),
                CombatSchools.PHYSICAL.id(),
                magnitude
        );
    }
}
