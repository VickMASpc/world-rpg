package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.CombatSimulationScenario;

/**
 * First representative P4.4 routine encounter.
 */
public final class RoutineSameLevelCasterScenario
        implements CombatSimulationScenario {
    public static final int LEVEL =
            RoutineCasterCalibrationSession.LEVEL;
    public static final ResourceKey HEALTH =
            RoutineCasterCalibrationSession.HEALTH;
    public static final ResourceKey MANA =
            RoutineCasterCalibrationSession.MANA;

    @Override
    public CombatSimulationReport run(long seed) {
        RoutineCasterCalibrationSession session =
                new RoutineCasterCalibrationSession(seed);

        session.fightRoutineEnemy();
        return session.report();
    }
}
