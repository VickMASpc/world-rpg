package dev.worldrpg.integration.minecraft;

import java.util.List;
import java.util.Objects;

/**
 * Fixed, deliberately provisional block layout for the first-province
 * physical topology test. This is not production world data.
 */
public final class ProvinceGrayboxLayout {
    private static final Point HOME = new Point(0, 0);
    private static final Point WILD = new Point(700, 0);
    private static final Point CORRIDOR = new Point(1_900, 0);
    private static final Point FORK = new Point(3_620, 0);
    private static final Point DANGER = new Point(2_760, 0);
    private static final Point REFUGE = new Point(5_800, 0);
    private static final Point REGIONAL_SETTLEMENT = new Point(3_620, 3_600);
    private static final Point BEYOND = new Point(9_800, 0);
    private static final Point WORKLAND = new Point(700, 800);
    private static final Point LOCAL_RUIN = new Point(700, -1_600);
    private static final Point UNFINISHED_SITE = new Point(3_100, 1_000);

    private static final List<Route> ROUTES = List.of(
            new Route(
                    "home-to-wild",
                    RouteKind.SAFE_ROAD,
                    List.of(HOME, WILD)
            ),
            new Route(
                    "wild-to-corridor",
                    RouteKind.SAFE_ROAD,
                    List.of(WILD, CORRIDOR)
            ),
            new Route(
                    "corridor-to-fork-safe",
                    RouteKind.SAFE_ROAD,
                    List.of(
                            CORRIDOR,
                            new Point(2_150, -600),
                            new Point(2_800, -600),
                            new Point(3_100, 0),
                            FORK
                    )
            ),
            new Route(
                    "fork-to-refuge-safe",
                    RouteKind.SAFE_ROAD,
                    List.of(
                            FORK,
                            new Point(3_620, 300),
                            new Point(4_100, 300),
                            new Point(4_500, 0),
                            REFUGE
                    )
            ),
            new Route(
                    "refuge-to-beyond",
                    RouteKind.OUTWARD,
                    List.of(REFUGE, BEYOND)
            ),
            new Route(
                    "fork-to-regional-settlement",
                    RouteKind.SAFE_ROAD,
                    List.of(FORK, REGIONAL_SETTLEMENT)
            ),
            new Route(
                    "danger-shortcut-corridor-to-fork",
                    RouteKind.DANGEROUS_SHORTCUT,
                    List.of(CORRIDOR, DANGER, FORK)
            ),
            new Route(
                    "learned-regional-return-shortcut",
                    RouteKind.LEARNED_SHORTCUT,
                    List.of(REGIONAL_SETTLEMENT, WILD)
            ),
            new Route(
                    "workland-detour",
                    RouteKind.DETOUR,
                    List.of(WILD, WORKLAND)
            ),
            new Route(
                    "local-ruin-detour",
                    RouteKind.DETOUR,
                    List.of(WILD, LOCAL_RUIN)
            ),
            new Route(
                    "unfinished-danger-site",
                    RouteKind.DANGEROUS_SHORTCUT,
                    List.of(DANGER, UNFINISHED_SITE)
            )
    );

    private static final List<Landmark> LANDMARKS = List.of(
            new Landmark(Node.HOME, HOME, -6, -6, 5),
            new Landmark(Node.WILD, WILD, -6, -6, 6),
            new Landmark(Node.CORRIDOR, CORRIDOR, -6, -6, 8),
            new Landmark(Node.FORK, FORK, -6, 8, 10),
            new Landmark(Node.DANGER, DANGER, 0, 9, 18),
            new Landmark(Node.REFUGE, REFUGE, -6, -6, 8),
            new Landmark(Node.REGIONAL_SETTLEMENT, REGIONAL_SETTLEMENT, 9, 0, 12),
            new Landmark(Node.BEYOND, BEYOND, 0, 9, 10),
            new Landmark(Node.WORKLAND, WORKLAND, 6, 6, 4),
            new Landmark(Node.LOCAL_RUIN, LOCAL_RUIN, 6, -6, 5),
            new Landmark(Node.UNFINISHED_SITE, UNFINISHED_SITE, 6, 6, 6)
    );

    private ProvinceGrayboxLayout() {
    }

    public static List<Route> routes() {
        return ROUTES;
    }

    public static List<Landmark> landmarks() {
        return LANDMARKS;
    }

    public static Route route(String id) {
        Objects.requireNonNull(id, "id");
        return ROUTES.stream()
                .filter(route -> route.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown province graybox route: " + id
                ));
    }

    public static double routeLength(String id) {
        return routeLength(route(id));
    }

    public static double routeLength(Route route) {
        Objects.requireNonNull(route, "route");
        double length = 0.0;
        for (int i = 1; i < route.points().size(); i++) {
            Point from = route.points().get(i - 1);
            Point to = route.points().get(i);
            length += Math.hypot(
                    to.east() - from.east(),
                    to.south() - from.south()
            );
        }
        return length;
    }

    public enum RouteKind {
        SAFE_ROAD(3),
        DANGEROUS_SHORTCUT(2),
        LEARNED_SHORTCUT(1),
        DETOUR(2),
        OUTWARD(3);

        private final int width;

        RouteKind(int width) {
            this.width = width;
        }

        public int width() {
            return width;
        }
    }

    public enum Node {
        HOME,
        WILD,
        CORRIDOR,
        FORK,
        DANGER,
        REFUGE,
        REGIONAL_SETTLEMENT,
        BEYOND,
        WORKLAND,
        LOCAL_RUIN,
        UNFINISHED_SITE
    }

    public record Point(int east, int south) {
    }

    public record Route(String id, RouteKind kind, List<Point> points) {
        public Route {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(kind, "kind");
            points = List.copyOf(points);
            if (points.size() < 2) {
                throw new IllegalArgumentException(
                        "a graybox route must contain at least two points"
                );
            }
        }
    }

    public record Landmark(
            Node node,
            Point position,
            int markerEast,
            int markerSouth,
            int height
    ) {
        public Landmark {
            Objects.requireNonNull(node, "node");
            Objects.requireNonNull(position, "position");
            if (height < 1) {
                throw new IllegalArgumentException(
                        "landmark height must be positive"
                );
            }
        }
    }
}
