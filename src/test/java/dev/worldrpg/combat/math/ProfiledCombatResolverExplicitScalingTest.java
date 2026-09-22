package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatPowerScaling;
import dev.worldrpg.combat.resolution.CombatPowerTerm;
import dev.worldrpg.combat.resolution.CombatResolutionProfileIds;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfiledCombatResolverExplicitScalingTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    @Test
    void physicalSchoolCanScaleFromSpellPowerExplicitly() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                target();

        source.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                500.0
        );
        source.stats().setBase(
                CombatMathStats.SPELL_POWER,
                40.0
        );

        CombatMagnitudeResolvedEvent event =
                resolved(
                        source,
                        target,
                        CombatSchools.PHYSICAL.id(),
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.SPELL_POWER,
                                        0.5
                                )
                        )
                );

        assertEquals(
                20.0,
                event.trace().powerContribution(),
                0.0
        );
        assertEquals(
                30.0,
                event.trace().requestedFinal(),
                0.0
        );
    }

    @Test
    void fireSchoolCanScaleFromAttackPowerExplicitly() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                target();

        source.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                40.0
        );
        source.stats().setBase(
                CombatMathStats.SPELL_POWER,
                500.0
        );

        CombatMagnitudeResolvedEvent event =
                resolved(
                        source,
                        target,
                        CombatSchools.FIRE.id(),
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.25
                                )
                        )
                );

        assertEquals(
                10.0,
                event.trace().powerContribution(),
                0.0
        );
        assertEquals(
                20.0,
                event.trace().requestedFinal(),
                0.0
        );
    }

    @Test
    void multipleStatsCanContributeWithoutSchoolInference() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                target();

        source.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                20.0
        );
        source.stats().setBase(
                CombatMathStats.SPELL_POWER,
                30.0
        );

        CombatMagnitudeResolvedEvent event =
                resolved(
                        source,
                        target,
                        CombatSchools.SHADOW.id(),
                        CombatPowerScaling.explicit(
                                new CombatPowerTerm(
                                        CombatMathStats.ATTACK_POWER,
                                        0.5
                                ),
                                new CombatPowerTerm(
                                        CombatMathStats.SPELL_POWER,
                                        0.5
                                )
                        )
                );

        assertEquals(
                25.0,
                event.trace().powerContribution(),
                0.0
        );
    }

    private static CombatMagnitudeResolvedEvent resolved(
            CombatActor source,
            CombatActor target,
            RpgId school,
            CombatPowerScaling scaling
    ) {
        ProfiledCombatResolver resolver =
                new ProfiledCombatResolver(
                        new CombatMathProfile(
                                HEALTH,
                                0.99,
                                0.99,
                                0.99,
                                2.0,
                                1.0,
                                100.0,
                                0.75
                        ),
                        () -> 0.5
                );

        return (CombatMagnitudeResolvedEvent) resolver.resolve(
                new CombatMagnitudeRequest(
                        0,
                        CombatMagnitudeKind.DAMAGE,
                        source,
                        target,
                        RpgId.parse(
                                "world_rpg:ability/test/explicit_scaling"
                        ),
                        school,
                        CombatResolutionProfileIds.GUARANTEED,
                        scaling,
                        10.0
                )
        ).get(0);
    }

    private static CombatActor target() {
        CombatActor target =
                new CombatActor(new CombatActorId(2));
        target.resources().add(
                HEALTH,
                1_000.0,
                1_000.0
        );
        return target;
    }
}
