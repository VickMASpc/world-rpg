package dev.worldrpg.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.aura.AuraRefreshPolicy;
import dev.worldrpg.combat.aura.AuraStatModifier;
import dev.worldrpg.combat.aura.AuraUniqueness;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.effect.ApplyAuraEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.stat.ModifierSource;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mechanism proof only. None of these numbers are balance targets.
 */
class P3KernelProofTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/proof_mana");
    private static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/proof_health");
    private static final StatKey POWER =
            StatKey.of("world_rpg:stat/proof_power");

    @Test
    void classFixtureComposesThreeAbilitiesAuraItemAndPersistentResources() {
        CombatActor player = new CombatActor(new CombatActorId(1));
        player.resources().add(MANA, 100.0, 100.0);
        player.resources().add(HEALTH, 100.0, 100.0);
        player.stats().setBase(POWER, 100.0);

        CombatActor hostile = new CombatActor(new CombatActorId(2));
        hostile.resources().add(HEALTH, 100.0, 100.0);

        // One item instance participates in the same generic stat system as auras.
        RpgId staffId = RpgId.parse("world_rpg:item/proof/apprentice_staff");
        player.stats().addModifier(
                POWER,
                new ModifierSource(staffId, 5001),
                StatModifierOperation.ADD,
                10.0,
                0
        );
        assertEquals(110.0, player.stats().value(POWER));

        AuraDefinition focusAura = new AuraDefinition(
                RpgId.parse("world_rpg:aura/proof/focus"),
                1,
                OptionalLong.of(50),
                AuraUniqueness.PER_TARGET,
                AuraRefreshPolicy.RESET_DURATION,
                List.of(new AuraStatModifier(
                        POWER,
                        StatModifierOperation.ADD,
                        20.0,
                        10
                ))
        );

        AbilityDefinition focus = ability(
                "world_rpg:ability/proof/focus",
                AbilityCastKind.INSTANT,
                0,
                OptionalLong.empty(),
                10.0,
                new EffectSequence(List.of(
                        new ApplyAuraEffect(EffectRecipient.SOURCE, focusAura)
                ))
        );

        AbilityDefinition bolt = ability(
                "world_rpg:ability/proof/bolt",
                AbilityCastKind.TIMED,
                4,
                OptionalLong.empty(),
                20.0,
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                HEALTH,
                                15.0
                        )
                ))
        );

        AbilityDefinition channel = ability(
                "world_rpg:ability/proof/channel",
                AbilityCastKind.CHANNEL,
                6,
                OptionalLong.of(2),
                30.0,
                new EffectSequence(List.of(
                        new ResourceDrainEffect(
                                EffectRecipient.TARGET,
                                HEALTH,
                                5.0
                        )
                ))
        );

        ProofClass fixtureClass = new ProofClass(
                RpgId.parse("world_rpg:class/proof_caster"),
                List.of(focus, bolt, channel)
        );
        assertEquals(3, fixtureClass.abilities().size());

        CastController casts = new CastController(player);

        assertTrue(casts.tryActivate(focus, hostile, 0).accepted());
        assertEquals(90.0, player.resources().require(MANA).current());
        assertEquals(130.0, player.stats().value(POWER));

        assertTrue(casts.tryActivate(bolt, hostile, 10).accepted());
        assertEquals(70.0, player.resources().require(MANA).current());
        assertEquals(100.0, hostile.resources().require(HEALTH).current());

        casts.tick(14);
        assertEquals(85.0, hostile.resources().require(HEALTH).current());

        assertTrue(casts.tryActivate(channel, hostile, 20).accepted());
        assertEquals(40.0, player.resources().require(MANA).current());

        casts.tick(22);
        casts.tick(24);
        casts.tick(26);

        assertEquals(70.0, hostile.resources().require(HEALTH).current());
        assertEquals(40.0, player.resources().require(MANA).current());
        assertEquals(130.0, player.stats().value(POWER));
        assertTrue(casts.activeCast().isEmpty());
    }

    private static AbilityDefinition ability(
            String id,
            AbilityCastKind kind,
            long duration,
            OptionalLong channelInterval,
            double manaCost,
            EffectSequence effects
    ) {
        return new AbilityDefinition(
                RpgId.parse(id),
                kind,
                duration,
                channelInterval,
                0,
                0,
                List.of(new AbilityCost(MANA, manaCost)),
                context -> ConditionResult.pass(),
                effects
        );
    }

    private record ProofClass(
            RpgId id,
            List<AbilityDefinition> abilities
    ) {
        private ProofClass {
            abilities = List.copyOf(abilities);
        }
    }
}
