package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfiledCombatResolverTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    private static final CombatMathProfile PROFILE =
            new CombatMathProfile(
                    HEALTH,
                    0.5,
                    0.5,
                    0.5,
                    2.0,
                    1.0,
                    100.0,
                    0.75
            );

    @Test
    void physicalDamagePipelineIsOrderedAndReportsOverkill() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        source.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                40.0
        );
        source.stats().setBase(
                CombatMathStats.DAMAGE_DONE_BONUS,
                0.25
        );
        source.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                1.0
        );

        target.stats().setBase(
                CombatMathStats.ARMOR,
                100.0
        );
        target.stats().setBase(
                CombatMathStats.DAMAGE_TAKEN_BONUS,
                0.20
        );
        target.resources().add(
                HEALTH,
                100.0,
                50.0
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        PROFILE,
                        () -> 0.25
                );

        var events = resolver.resolve(
                new CombatMagnitudeRequest(
                        10,
                        CombatMagnitudeKind.DAMAGE,
                        source,
                        target,
                        RpgId.parse("world_rpg:ability/test/strike"),
                        CombatSchools.PHYSICAL.id(),
                        20.0
                )
        );

        assertEquals(2, events.size());
        assertTrue(events.get(0)
                instanceof CombatMagnitudeResolvedEvent);
        assertTrue(events.get(1)
                instanceof ResourceChangedEvent);

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) events.get(0);
        CombatResolutionTrace trace = event.trace();

        assertEquals(20.0, trace.authoredBase(), 0.0);
        assertEquals(20.0, trace.powerContribution(), 0.0);
        assertEquals(40.0, trace.afterScaling(), 0.0);
        assertEquals(1.25, trace.outgoingMultiplier(), 0.0);
        assertEquals(50.0, trace.afterOutgoing(), 0.0);
        assertTrue(trace.critical());
        assertEquals(100.0, trace.afterCritical(), 0.0);
        assertEquals(0.5, trace.mitigationFraction(), 0.0);
        assertEquals(50.0, trace.afterMitigation(), 0.0);
        assertEquals(1.2, trace.incomingMultiplier(), 0.0);
        assertEquals(60.0, trace.requestedFinal(), 1.0e-12);
        assertEquals(50.0, trace.appliedFinal(), 0.0);
        assertEquals(10.0, trace.excess(), 1.0e-12);
        assertEquals(0.0, target.resources().require(HEALTH).current(), 0.0);
    }

    @Test
    void healingUsesSameResolverAndReportsOverheal() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        source.stats().setBase(
                CombatMathStats.HEALING_POWER,
                20.0
        );
        source.stats().setBase(
                CombatMathStats.HEALING_DONE_BONUS,
                0.25
        );
        target.stats().setBase(
                CombatMathStats.HEALING_TAKEN_BONUS,
                0.20
        );
        target.resources().add(
                HEALTH,
                100.0,
                80.0
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        PROFILE,
                        () -> {
                            throw new AssertionError(
                                    "zero crit chance must not consume a roll"
                            );
                        }
                );

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        new CombatMagnitudeRequest(
                                20,
                                CombatMagnitudeKind.HEALING,
                                source,
                                target,
                                RpgId.parse("world_rpg:ability/test/heal"),
                                CombatSchools.HOLY.id(),
                                10.0
                        )
                ).get(0);

        CombatResolutionTrace trace = event.trace();

        assertEquals(20.0, trace.afterScaling(), 0.0);
        assertEquals(25.0, trace.afterOutgoing(), 0.0);
        assertEquals(0.0, trace.mitigationFraction(), 0.0);
        assertEquals(30.0, trace.requestedFinal(), 1.0e-12);
        assertEquals(20.0, trace.appliedFinal(), 0.0);
        assertEquals(10.0, trace.excess(), 1.0e-12);
        assertEquals(100.0, target.resources().require(HEALTH).current(), 0.0);
    }

    @Test
    void mitigationCapPreventsDefenseFromReachingImmunity() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.stats().setBase(
                CombatMathStats.FROST_RESISTANCE,
                100000.0
        );
        target.resources().add(
                HEALTH,
                100.0,
                100.0
        );

        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        PROFILE,
                        () -> 0.5
                );

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        new CombatMagnitudeRequest(
                                30,
                                CombatMagnitudeKind.DAMAGE,
                                source,
                                target,
                                RpgId.parse("world_rpg:ability/test/frost"),
                                CombatSchools.FROST.id(),
                                40.0
                        )
                ).get(0);

        assertEquals(
                0.75,
                event.trace().mitigationFraction(),
                0.0
        );
        assertEquals(
                10.0,
                event.trace().requestedFinal(),
                0.0
        );
    }
}
