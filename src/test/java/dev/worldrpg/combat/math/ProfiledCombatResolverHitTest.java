package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfiledCombatResolverHitTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    private static final CombatMathProfile PROFILE =
            new CombatMathProfile(
                    HEALTH,
                    0.0,
                    0.0,
                    0.0,
                    2.0,
                    1.0,
                    100.0,
                    0.75
            );

    @Test
    void missShortCircuitsBeforeCritAndHealthMutation() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);

        source.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                1.0
        );

        AtomicInteger rollsConsumed =
                new AtomicInteger();

        ProfiledCombatResolver resolver =
                resolver(
                        () -> {
                            int index = rollsConsumed.getAndIncrement();
                            if (index > 0) {
                                throw new AssertionError(
                                        "miss must not consume a crit roll"
                                );
                            }
                            return 0.01;
                        }
                );

        var events = resolver.resolve(
                request(
                        source,
                        target,
                        CombatResolutionProfileIds.DIRECT_WEAPON
                )
        );

        assertEquals(1, events.size());
        assertEquals(1, rollsConsumed.get());

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) events.get(0);

        assertEquals(
                CombatContactOutcome.MISS,
                event.trace().contactOutcome()
        );
        assertFalse(event.trace().critical());
        assertEquals(
                0.05,
                event.trace().missChance(),
                0.0
        );
        assertEquals(
                100.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    @Test
    void hitBonusCanRemoveBaselineMissWithoutConsumingContactRoll() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);

        source.stats().setBase(
                CombatMathStats.HIT_CHANCE,
                0.05
        );

        ProfiledCombatResolver resolver =
                resolver(() -> {
                    throw new AssertionError(
                            "zero miss and zero crit must consume no roll"
                    );
                });

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        request(
                                source,
                                target,
                                CombatResolutionProfileIds.DIRECT_WEAPON
                        )
                ).get(0);

        assertEquals(
                CombatContactOutcome.HIT,
                event.trace().contactOutcome()
        );
        assertEquals(0.0, event.trace().missChance(), 0.0);
        assertTrue(event.trace().contactRoll().isEmpty());
        assertEquals(
                90.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    @Test
    void periodicProfileDisablesCritEvenWithOneHundredPercentCritStat() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);

        source.stats().setBase(
                CombatMathStats.CRIT_CHANCE,
                1.0
        );

        ProfiledCombatResolver resolver =
                resolver(() -> {
                    throw new AssertionError(
                            "periodic draft consumes no contact/crit roll"
                    );
                });

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        request(
                                source,
                                target,
                                CombatResolutionProfileIds.PERIODIC
                        )
                ).get(0);

        assertEquals(
                CombatContactOutcome.HIT,
                event.trace().contactOutcome()
        );
        assertEquals(0.0, event.trace().criticalChance(), 0.0);
        assertFalse(event.trace().critical());
    }

    @Test
    void unknownProfileFailsBeforeMutation() {
        CombatActor source = actor(1, 100.0);
        CombatActor target = actor(2, 100.0);

        ProfiledCombatResolver resolver =
                resolver(() -> 0.5);

        assertThrows(
                NoSuchElementException.class,
                () -> resolver.resolve(
                        request(
                                source,
                                target,
                                RpgId.parse(
                                        "world_rpg:resolution/missing"
                                )
                        )
                )
        );

        assertEquals(
                100.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }

    private static ProfiledCombatResolver resolver(
            CombatRollSource rolls
    ) {
        return new ProfiledCombatResolver(
                PROFILE,
                rolls,
                CombatStatResolver.direct(),
                CombatLevelSource.constant(1),
                WorldRpgOutcomeDraft.referenceProfiles()
        );
    }

    private static CombatMagnitudeRequest request(
            CombatActor source,
            CombatActor target,
            RpgId profileId
    ) {
        return new CombatMagnitudeRequest(
                0,
                CombatMagnitudeKind.DAMAGE,
                source,
                target,
                RpgId.parse("world_rpg:ability/test/hit"),
                CombatSchools.PHYSICAL.id(),
                profileId,
                10.0
        );
    }

    private static CombatActor actor(
            long id,
            double health
    ) {
        CombatActor actor =
                new CombatActor(new CombatActorId(id));
        actor.resources().add(
                HEALTH,
                health,
                health
        );
        return actor;
    }
}
