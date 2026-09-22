package dev.worldrpg.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class P3ProofScenarioTest {
    @Test
    void runtimeProofMatchesKernelContract() {
        P3ProofReport report = P3ProofScenario.run();

        assertTrue(report.passed());
        assertEquals(40.0, report.playerMana());
        assertEquals(130.0, report.playerPower());
        assertEquals(70.0, report.targetHealth());
        assertEquals(1, report.activeAuras());
        assertTrue(report.castIdle());
    }
}
