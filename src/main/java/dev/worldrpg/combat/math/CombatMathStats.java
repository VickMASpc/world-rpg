package dev.worldrpg.combat.math;

import dev.worldrpg.combat.stat.StatKey;

import java.util.Map;

public final class CombatMathStats {
    public static final StatKey ATTACK_POWER =
            StatKey.of("world_rpg:stat/attack_power");
    public static final StatKey SPELL_POWER =
            StatKey.of("world_rpg:stat/spell_power");
    public static final StatKey HEALING_POWER =
            StatKey.of("world_rpg:stat/healing_power");
    public static final StatKey CRIT_CHANCE =
            StatKey.of("world_rpg:stat/crit_chance");

    public static final StatKey ARMOR =
            StatKey.of("world_rpg:stat/armor");
    public static final StatKey FIRE_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/fire");
    public static final StatKey FROST_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/frost");
    public static final StatKey ARCANE_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/arcane");
    public static final StatKey NATURE_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/nature");
    public static final StatKey HOLY_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/holy");
    public static final StatKey SHADOW_RESISTANCE =
            StatKey.of("world_rpg:stat/resistance/shadow");

    public static final StatKey DAMAGE_DONE_BONUS =
            StatKey.of("world_rpg:stat/damage_done_bonus");
    public static final StatKey DAMAGE_TAKEN_BONUS =
            StatKey.of("world_rpg:stat/damage_taken_bonus");
    public static final StatKey HEALING_DONE_BONUS =
            StatKey.of("world_rpg:stat/healing_done_bonus");
    public static final StatKey HEALING_TAKEN_BONUS =
            StatKey.of("world_rpg:stat/healing_taken_bonus");

    private static final Map<CombatSchoolKey, StatKey> RESISTANCE_BY_SCHOOL =
            Map.of(
                    CombatSchools.FIRE, FIRE_RESISTANCE,
                    CombatSchools.FROST, FROST_RESISTANCE,
                    CombatSchools.ARCANE, ARCANE_RESISTANCE,
                    CombatSchools.NATURE, NATURE_RESISTANCE,
                    CombatSchools.HOLY, HOLY_RESISTANCE,
                    CombatSchools.SHADOW, SHADOW_RESISTANCE
            );

    private CombatMathStats() {
    }

    public static StatKey resistanceFor(CombatSchoolKey school) {
        if (school.equals(CombatSchools.PHYSICAL)) {
            return ARMOR;
        }

        StatKey stat = RESISTANCE_BY_SCHOOL.get(school);
        if (stat == null) {
            throw new IllegalArgumentException(
                    "No baseline resistance stat for school " + school
            );
        }
        return stat;
    }
}
