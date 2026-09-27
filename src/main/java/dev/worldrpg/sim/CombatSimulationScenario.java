package dev.worldrpg.sim;

/**
 * One deterministic simulation scenario parameterized only by an explicit
 * random seed.
 */
@FunctionalInterface
public interface CombatSimulationScenario {
    CombatSimulationReport run(long seed);
}
