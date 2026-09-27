package dev.worldrpg.combat.math;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.event.CombatMagnitudeResolvedEvent;
import dev.worldrpg.combat.resolution.CombatMagnitudeKind;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resource.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfiledCombatResolverLevelMitigationTest {
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/health");

    @Test
    void sourceLevelControlsMitigationScaleWithoutChangingArmorStat() {
        CombatActor source =
                new CombatActor(new CombatActorId(1));
        CombatActor target =
                new CombatActor(new CombatActorId(2));

        target.resources().add(HEALTH, 500.0, 500.0);
        target.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(30)
        );

        CombatMathProfile profile =
                new CombatMathProfile(
                        HEALTH,
                        0.0,
                        0.0,
                        0.0,
                        2.0,
                        1.0,
                        WorldRpgDefenseDraft.MITIGATION_SCALE,
                        0.75
                );

        ProfiledCombatResolver level30 =
                new ProfiledCombatResolver(
                        profile,
                        () -> 0.5,
                        CombatStatResolver.direct(),
                        actor -> 30
                );

        CombatMagnitudeResolvedEvent sameLevel =
                (CombatMagnitudeResolvedEvent) level30.resolve(
                        request(source, target, 0)
                ).get(0);

        target.resources().require(HEALTH).setCurrent(500.0);

        ProfiledCombatResolver level60 =
                new ProfiledCombatResolver(
                        profile,
                        () -> 0.5,
                        CombatStatResolver.direct(),
                        actor -> actor.id().equals(source.id())
                                ? 60
                                : 30
                );

        CombatMagnitudeResolvedEvent higherAttacker =
                (CombatMagnitudeResolvedEvent) level60.resolve(
                        request(source, target, 1)
                ).get(0);

        assertEquals(
                0.25,
                sameLevel.trace().mitigationFraction(),
                1.0e-12
        );
        assertTrue(
                higherAttacker.trace().mitigationFraction()
                        < sameLevel.trace().mitigationFraction()
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
                RpgId.parse("world_rpg:ability/test/level_mitigation"),
                CombatSchools.PHYSICAL.id(),
                100.0
        );
    }
}
