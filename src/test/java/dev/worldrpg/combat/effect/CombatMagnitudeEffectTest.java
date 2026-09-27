package dev.worldrpg.combat.effect;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.math.CombatLevelSource;
import dev.worldrpg.combat.math.CombatMathProfile;
import dev.worldrpg.combat.math.CombatOutcomeProfileSet;
import dev.worldrpg.combat.math.CombatSchools;
import dev.worldrpg.combat.math.CombatStatResolver;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CombatMagnitudeEffectTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/mana");

    @Test
    void invalidMagnitudePreflightProtectsEarlierEffectsFromMutation() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));
        source.resources().add(MANA, 100.0, 100.0);

        AtomicInteger resolveCalls = new AtomicInteger();

        CombatResolutionGateway rejected =
                new CombatResolutionGateway() {
                    @Override
                    public ConditionResult validate(
                            dev.worldrpg.combat.resolution.CombatMagnitudeRequest request
                    ) {
                        return ConditionResult.fail(
                                RpgId.parse(
                                        "world_rpg:condition/test_rejected_magnitude"
                                ),
                                "rejected for test"
                        );
                    }

                    @Override
                    public List<dev.worldrpg.combat.event.CombatEvent> resolve(
                            dev.worldrpg.combat.resolution.CombatMagnitudeRequest request
                    ) {
                        resolveCalls.incrementAndGet();
                        throw new AssertionError(
                                "rejected effect must never resolve"
                        );
                    }
                };

        EffectSequence sequence =
                new EffectSequence(
                        List.of(
                                new ResourceDrainEffect(
                                        EffectRecipient.SOURCE,
                                        MANA,
                                        10.0
                                ),
                                new CombatMagnitudeEffect(
                                        EffectRecipient.TARGET,
                                        CombatMagnitudeKind.DAMAGE,
                                        RpgId.parse(
                                                "world_rpg:ability/test/preflight"
                                        ),
                                        CombatSchools.PHYSICAL.id(),
                                        CombatResolutionProfileIds.GUARANTEED,
                                        10.0,
                                        rejected
                                )
                        )
                );

        EffectSequenceResult result =
                sequence.execute(
                        new EffectContext(
                                source,
                                target,
                                0
                        )
                );

        assertFalse(result.applied());
        assertEquals(0, resolveCalls.get());
        assertEquals(
                100.0,
                source.resources().require(MANA).current(),
                0.0
        );
    }

    @Test
    void validMagnitudeEffectUsesProductionResolver() {
        CombatActor source = new CombatActor(new CombatActorId(1));
        CombatActor target = new CombatActor(new CombatActorId(2));
        target.resources().add(HEALTH, 100.0, 100.0);

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
                        () -> 0.5,
                        CombatStatResolver.direct(),
                        CombatLevelSource.constant(1),
                        CombatOutcomeProfileSet.guaranteedOnly()
                );

        CombatMagnitudeEffect effect =
                new CombatMagnitudeEffect(
                        EffectRecipient.TARGET,
                        CombatMagnitudeKind.DAMAGE,
                        RpgId.parse(
                                "world_rpg:ability/test/magnitude"
                        ),
                        CombatSchools.PHYSICAL.id(),
                        CombatResolutionProfileIds.GUARANTEED,
                        25.0,
                        resolver
                );

        EffectSequenceResult result =
                new EffectSequence(
                        List.of(effect)
                ).execute(
                        new EffectContext(
                                source,
                                target,
                                0
                        )
                );

        assertEquals(2, result.events().size());
        assertEquals(
                75.0,
                target.resources().require(HEALTH).current(),
                0.0
        );
    }
}
