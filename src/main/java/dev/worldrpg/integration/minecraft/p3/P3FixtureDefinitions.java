package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.aura.AuraDefinition;
import dev.worldrpg.combat.aura.AuraRefreshPolicy;
import dev.worldrpg.combat.aura.AuraStatModifier;
import dev.worldrpg.combat.aura.AuraUniqueness;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.condition.Conditions;
import dev.worldrpg.combat.effect.ApplyAuraEffect;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.effect.EffectSequence;
import dev.worldrpg.combat.effect.ResourceDrainEffect;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;
import dev.worldrpg.combat.target.TargetConditions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.OptionalLong;

/**
 * Developer-only P3 fixture definitions.
 *
 * <p>Every numeric value here exists to exercise mechanism behavior. None of
 * these values are P4 balance decisions.</p>
 */
public final class P3FixtureDefinitions {
    public static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/proof_mana");
    public static final ResourceKey HEALTH =
            ResourceKey.of("world_rpg:resource/proof_health");
    public static final StatKey POWER =
            StatKey.of("world_rpg:stat/proof_power");

    public static final RpgId STAFF =
            RpgId.parse("world_rpg:item/proof/apprentice_staff");
    public static final RpgId FOCUS =
            RpgId.parse("world_rpg:ability/proof/focus");
    public static final RpgId BOLT =
            RpgId.parse("world_rpg:ability/proof/bolt");
    public static final RpgId CHANNEL =
            RpgId.parse("world_rpg:ability/proof/channel");

    private static final AuraDefinition FOCUS_AURA =
            new AuraDefinition(
                    RpgId.parse("world_rpg:aura/proof/focus"),
                    1,
                    OptionalLong.of(200),
                    AuraUniqueness.PER_TARGET,
                    AuraRefreshPolicy.RESET_DURATION,
                    List.of(new AuraStatModifier(
                            POWER,
                            StatModifierOperation.ADD,
                            20.0,
                            10
                    ))
            );

    private static final Map<RpgId, AbilityDefinition> ABILITIES =
            createAbilities();

    private P3FixtureDefinitions() {
    }

    public static AbilityDefinition requireAbility(RpgId id) {
        AbilityDefinition ability = ABILITIES.get(id);
        if (ability == null) {
            throw new NoSuchElementException(
                    "Unknown P3 fixture ability: " + id
            );
        }
        return ability;
    }

    public static RpgId parseShortAbility(String shortName) {
        return switch (shortName) {
            case "focus" -> FOCUS;
            case "bolt" -> BOLT;
            case "channel" -> CHANNEL;
            default -> throw new NoSuchElementException(
                    "Unknown P3 fixture ability name: " + shortName
            );
        };
    }

    public static List<String> shortNames() {
        return List.of("focus", "bolt", "channel");
    }

    private static Map<RpgId, AbilityDefinition> createAbilities() {
        Map<RpgId, AbilityDefinition> abilities = new LinkedHashMap<>();

        abilities.put(
                FOCUS,
                new AbilityDefinition(
                        FOCUS,
                        AbilityCastKind.INSTANT,
                        0,
                        OptionalLong.empty(),
                        0,
                        0,
                        List.of(new AbilityCost(MANA, 10.0)),
                        context -> context.target() == context.source()
                                ? ConditionResult.pass()
                                : ConditionResult.fail(
                                        RpgId.parse(
                                                "world_rpg:condition/proof_focus_self_only"
                                        ),
                                        "Proof Focus is self-only"
                                ),
                        new EffectSequence(List.of(
                                new ApplyAuraEffect(
                                        EffectRecipient.SOURCE,
                                        FOCUS_AURA
                                )
                        ))
                )
        );

        abilities.put(
                BOLT,
                new AbilityDefinition(
                        BOLT,
                        AbilityCastKind.TIMED,
                        20,
                        OptionalLong.empty(),
                        10,
                        5,
                        List.of(new AbilityCost(MANA, 20.0)),
                        hostileTargetConditions(12.0),
                        new EffectSequence(List.of(
                                new ResourceDrainEffect(
                                        EffectRecipient.TARGET,
                                        HEALTH,
                                        15.0
                                )
                        ))
                )
        );

        abilities.put(
                CHANNEL,
                new AbilityDefinition(
                        CHANNEL,
                        AbilityCastKind.CHANNEL,
                        60,
                        OptionalLong.of(20),
                        20,
                        5,
                        List.of(new AbilityCost(MANA, 30.0)),
                        hostileTargetConditions(10.0),
                        new EffectSequence(List.of(
                                new ResourceDrainEffect(
                                        EffectRecipient.TARGET,
                                        HEALTH,
                                        5.0
                                )
                        ))
                )
        );

        return Map.copyOf(abilities);
    }

    private static dev.worldrpg.combat.condition.Condition<
            dev.worldrpg.combat.ability.AbilityContext
            > hostileTargetConditions(double maxRange) {
        return Conditions.all(
                TargetConditions.requireSourceAlive(),
                TargetConditions.requireTargetAlive(),
                TargetConditions.requireSameWorld(),
                TargetConditions.disallowSelf(),
                TargetConditions.requireLineOfSight(),
                TargetConditions.maxRange(maxRange)
        );
    }
}
