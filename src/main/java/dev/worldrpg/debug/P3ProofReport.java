package dev.worldrpg.debug;

public record P3ProofReport(
        double playerMana,
        double playerPower,
        double targetHealth,
        int activeAuras,
        boolean castIdle
) {
    public boolean passed() {
        return playerMana == 40.0
                && playerPower == 130.0
                && targetHealth == 70.0
                && activeAuras == 1
                && castIdle;
    }

    public String summary() {
        return "P3 proof "
                + (passed() ? "PASS" : "FAIL")
                + " | mana=" + playerMana
                + " power=" + playerPower
                + " targetHealth=" + targetHealth
                + " auras=" + activeAuras
                + " castIdle=" + castIdle;
    }
}
