package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.content.combat.P3CombatContentRuntime;

import java.util.List;
import java.util.NoSuchElementException;

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

    private P3FixtureDefinitions() {
    }

    public static AbilityDefinition requireAbility(RpgId id) {
        return P3CombatContentRuntime.active().requireAbility(id);
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
}
