package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfiledCombatResolverDerivedStatsTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    @Test
    void productionResolverCanConsumeDerivedCriticalChance() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        source.stats().setBase(
                CombatMathStats.CRIT_RATING,
                80.0
        );
        target.resources().add(HEALTH, 100.0, 100.0);

        CombatStatResolver stats =
                new ProfiledCombatStatResolver(
                        WorldRpgRatingDraft.referenceProfile(),
                        CombatLevelSource.constant(100)
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
                        () -> 0.05,
                        stats
                );

        CombatMagnitudeResolvedEvent event =
                (CombatMagnitudeResolvedEvent) resolver.resolve(
                        new CombatMagnitudeRequest(
                                0,
                                CombatMagnitudeKind.DAMAGE,
                                source,
                                target,
                                RpgId.parse("world_rpg:ability/test/rating_crit"),
                                CombatSchools.ARCANE.id(),
                                10.0
                        )
                ).get(0);

        // 80 rating at level 100 -> 10% crit. Roll 0.05 must crit.
        assertTrue(event.trace().critical());
    }
}
