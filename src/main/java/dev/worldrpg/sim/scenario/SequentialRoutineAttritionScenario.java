package dev.worldrpg.sim.scenario;

import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.sim.CombatSimulationReport;
import dev.worldrpg.sim.CombatSimulationScenario;

/**
 * Two routine encounters with no recovery/reset between them.
 */
public final class SequentialRoutineAttritionScenario
        implements CombatSimulationScenario {
    public static final ResourceKey HEALTH =
            RoutineCasterCalibrationSession.HEALTH;
    public static final ResourceKey MANA =
            RoutineCasterCalibrationSession.MANA;

    @Override
    public CombatSimulationReport run(long seed) {
        RoutineCasterCalibrationSession session =
                new RoutineCasterCalibrationSession(seed);

        session.fightRoutineEnemy();
        session.fightRoutineEnemy();

        return session.report();
    }
}
