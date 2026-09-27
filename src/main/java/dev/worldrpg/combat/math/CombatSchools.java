package dev.worldrpg.combat.math;

/**
 * Baseline production school vocabulary.
 *
 * <p>The keys remain namespaced data identities rather than a closed enum so
 * later systems can reference them uniformly with authored content.</p>
 */
public final class CombatSchools {
    public static final CombatSchoolKey PHYSICAL =
            CombatSchoolKey.of("world_rpg:school/physical");
    public static final CombatSchoolKey FIRE =
            CombatSchoolKey.of("world_rpg:school/fire");
    public static final CombatSchoolKey FROST =
            CombatSchoolKey.of("world_rpg:school/frost");
    public static final CombatSchoolKey ARCANE =
            CombatSchoolKey.of("world_rpg:school/arcane");
    public static final CombatSchoolKey NATURE =
            CombatSchoolKey.of("world_rpg:school/nature");
    public static final CombatSchoolKey HOLY =
            CombatSchoolKey.of("world_rpg:school/holy");
    public static final CombatSchoolKey SHADOW =
            CombatSchoolKey.of("world_rpg:school/shadow");

    private CombatSchools() {
    }
}
