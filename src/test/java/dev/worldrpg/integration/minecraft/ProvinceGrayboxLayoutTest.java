package dev.worldrpg.integration.minecraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProvinceGrayboxLayoutTest {
    private static final double REFERENCE_BLOCKS_PER_MINUTE = 250.0;

    @Test
    void safeRouteDistancesFitTheProvisionalTopologyBands() {
        double homeToFork = sum(
                "home-to-wild",
                "wild-to-corridor",
                "corridor-to-fork-safe"
        );
        double forkToRefuge =
                ProvinceGrayboxLayout.routeLength("fork-to-refuge-safe");
        double homeToRefuge = homeToFork + forkToRefuge;
        double homeToRegionalSettlement = homeToFork
                + ProvinceGrayboxLayout.routeLength(
                        "fork-to-regional-settlement"
                );

        assertWithinMinutes(homeToFork, 10.0, 18.0);
        assertWithinMinutes(forkToRefuge, 8.0, 15.0);
        assertWithinMinutes(homeToRefuge, 20.0, 30.0);
        assertWithinMinutes(homeToRegionalSettlement, 25.0, 40.0);
        assertWithinMinutes(
                ProvinceGrayboxLayout.routeLength("refuge-to-beyond"),
                15.0,
                30.0
        );
    }

    @Test
    void shortcutsSaveMeaningfulTimeAndRemainLongJourneys() {
        double homeToRegionalSettlement = sum(
                "home-to-wild",
                "wild-to-corridor",
                "corridor-to-fork-safe",
                "fork-to-regional-settlement"
        );
        double knownShortcutReturn =
                ProvinceGrayboxLayout.routeLength(
                        "learned-regional-return-shortcut"
                )
                        + ProvinceGrayboxLayout.routeLength("home-to-wild");
        double dangerousShortcut =
                ProvinceGrayboxLayout.routeLength(
                        "danger-shortcut-corridor-to-fork"
                );
        double safeCorridorLeg = ProvinceGrayboxLayout.routeLength(
                "corridor-to-fork-safe"
        );

        assertTrue(homeToRegionalSettlement - knownShortcutReturn >= 2_000.0);
        assertTrue(knownShortcutReturn >= 5_000.0);
        assertTrue(safeCorridorLeg - dangerousShortcut >= 700.0);
    }

    @Test
    void localDestinationsStayNearTheHomeRoad() {
        double homeToRuin = ProvinceGrayboxLayout.routeLength("home-to-wild")
                + ProvinceGrayboxLayout.routeLength("local-ruin-detour");

        assertEquals(
                700.0,
                ProvinceGrayboxLayout.routeLength("home-to-wild"),
                0.0
        );
        assertEquals(
                1_600.0,
                ProvinceGrayboxLayout.routeLength("local-ruin-detour"),
                0.0
        );
        assertWithinMinutes(homeToRuin, 6.0, 12.0);
        assertEquals(
                800.0,
                ProvinceGrayboxLayout.routeLength("workland-detour"),
                0.0
        );
    }

    private static double sum(String... routeIds) {
        double total = 0.0;
        for (String routeId : routeIds) {
            total += ProvinceGrayboxLayout.routeLength(routeId);
        }
        return total;
    }

    private static void assertWithinMinutes(
            double blocks,
            double minimumMinutes,
            double maximumMinutes
    ) {
        double minutes = blocks / REFERENCE_BLOCKS_PER_MINUTE;
        assertTrue(
                minutes >= minimumMinutes && minutes <= maximumMinutes,
                () -> blocks + " blocks estimates to " + minutes
                        + " minutes at the provisional reference pace"
        );
    }
}
