package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.CombatSimulationScenario;

/**
 * Two ordinary same-level enemies engaged simultaneously.
 */
public final class TwoMobAccidentalPullScenario
        implements CombatSimulationScenario {
    public static final ResourceKey HEALTH =
            RoutineCasterCalibrationSession.HEALTH;
    public static final ResourceKey MANA =
            RoutineCasterCalibrationSession.MANA;

    public static final CombatActorId PLAYER_ID =
            RoutineCasterCalibrationSession.PLAYER_ID;
    public static final CombatActorId FIRST_ENEMY_ID =
            RoutineCasterCalibrationSession.FIRST_ENEMY_ID;
    public static final CombatActorId SECOND_ENEMY_ID =
            RoutineCasterCalibrationSession.SECOND_ENEMY_ID;

    @Override
    public CombatSimulationReport run(long seed) {
        RoutineCasterCalibrationSession session =
                new RoutineCasterCalibrationSession(seed);

        session.fightTwoRoutineEnemies();
        return session.report();
    }
}
