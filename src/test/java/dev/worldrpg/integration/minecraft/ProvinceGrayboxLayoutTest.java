package dev.worldrpg.integration.minecraft;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProvinceGrayboxLayoutTest {
    private static final double REFERENCE_BLOCKS_PER_MINUTE = 250.0;

    @Test
    void safeRouteDistancesFitTheProvisionalTopologyBands() {
        assertJourneyWithinBand("a-to-r1-local");
        assertJourneyWithinBand("a-to-f-safe");
        assertJourneyWithinBand("a-to-g-safe");
        assertJourneyWithinBand("f-to-h-outward");
    }

    @Test
    void journeyDefinitionsMatchTheUnderlyingRouteGraph() {
        assertEquals(
                sum(
                        "home-to-wild",
                        "local-ruin-detour"
                ),
                ProvinceGrayboxLayout
                        .journey("a-to-r1-local")
                        .pathBlocks(),
                0.001
        );
        assertEquals(
                sum(
                        "home-to-wild",
                        "wild-to-corridor",
                        "corridor-to-fork-safe",
                        "fork-to-refuge-safe"
                ),
                ProvinceGrayboxLayout
                        .journey("a-to-f-safe")
                        .pathBlocks(),
                0.001
        );
        assertEquals(
                sum(
                        "home-to-wild",
                        "wild-to-corridor",
                        "corridor-to-fork-safe",
                        "fork-to-regional-settlement"
                ),
                ProvinceGrayboxLayout
                        .journey("a-to-g-safe")
                        .pathBlocks(),
                0.001
        );
    }

    @Test
    void shortcutsSaveMeaningfulTimeAndRemainLongJourneys() {
        double firstJourney =
                ProvinceGrayboxLayout
                        .journey("a-to-g-safe")
                        .pathBlocks();
        double knownReturn =
                ProvinceGrayboxLayout
                        .journey("g-to-a-learned")
                        .pathBlocks();
        double dangerousShortcut =
                ProvinceGrayboxLayout.routeLength(
                        "danger-shortcut-corridor-to-fork"
                );
        double safeCorridorLeg =
                ProvinceGrayboxLayout.routeLength(
                        "corridor-to-fork-safe"
                );

        assertTrue(
                firstJourney - knownReturn >= 2_000.0,
                "learned return must save a materially noticeable distance"
        );
        assertTrue(
                knownReturn >= 5_000.0,
                "knowledge must shorten the journey without deleting geography"
        );
        assertTrue(
                safeCorridorLeg - dangerousShortcut >= 700.0,
                "danger shortcut must trade safety for meaningful distance"
        );
    }

    @Test
    void localDestinationsStayNearTheHomeRoad() {
        assertEquals(
                700.0,
                ProvinceGrayboxLayout.routeLength(
                        "home-to-wild"
                ),
                0.0
        );
        assertEquals(
                1_600.0,
                ProvinceGrayboxLayout.routeLength(
                        "local-ruin-detour"
                ),
                0.0
        );
        assertEquals(
                800.0,
                ProvinceGrayboxLayout.routeLength(
                        "workland-detour"
                ),
                0.0
        );
    }

    @Test
    void nodePositionsRemainDistinctAndRegionalTownIsNotAdjacent() {
        var positions =
                new HashSet<
                        ProvinceGrayboxLayout.Point>();

        for (var node :
                ProvinceGrayboxLayout.Node.values()) {
            assertTrue(
                    positions.add(
                            ProvinceGrayboxLayout
                                    .nodePosition(node)
                    ),
                    () -> "duplicate topology node: " + node
            );
        }

        var home =
                ProvinceGrayboxLayout.nodePosition(
                        ProvinceGrayboxLayout.Node.HOME
                );
        var regional =
                ProvinceGrayboxLayout.nodePosition(
                        ProvinceGrayboxLayout.Node.REGIONAL_SETTLEMENT
                );
        assertTrue(
                Math.hypot(
                        regional.east() - home.east(),
                        regional.south() - home.south()
                ) > 4_500.0,
                "regional settlement must not read as an adjacent quest hub"
        );
    }

    private static double sum(String... routeIds) {
        double total = 0.0;
        for (String routeId : routeIds) {
            total += ProvinceGrayboxLayout.routeLength(
                    routeId
            );
        }
        return total;
    }

    private static void assertJourneyWithinBand(
            String journeyId
    ) {
        var journey =
                ProvinceGrayboxLayout.journey(journeyId);
        double minutes =
                journey.pathBlocks()
                        / REFERENCE_BLOCKS_PER_MINUTE;

        assertTrue(journey.hasTargetBand());
        assertTrue(
                minutes >= journey.targetMinMinutes()
                        && minutes
                        <= journey.targetMaxMinutes(),
                () -> journeyId
                        + " estimates to "
                        + minutes
                        + " minutes at the provisional reference pace"
        );
    }
}
