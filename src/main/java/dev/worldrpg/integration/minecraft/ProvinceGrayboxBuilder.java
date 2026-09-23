package dev.worldrpg.integration.minecraft;

import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Landmark;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Node;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Point;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Route;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.RouteKind;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/** Builds the disposable first-province route graybox for physical testing. */
public final class ProvinceGrayboxBuilder {
    private static final int RIVER_MIN_EAST = 3_580;
    private static final int RIVER_MAX_EAST = 3_620;
    private static final int RIVER_MIN_SOUTH = -200;
    private static final int RIVER_MAX_SOUTH = 200;
    private static final int BRIDGE_MIN_SOUTH = -2;
    private static final int BRIDGE_MAX_SOUTH = 2;
    private static final int FLATNESS_SAMPLE_BLOCKS = 256;

    private ProvinceGrayboxBuilder() {
    }

    public static BuildResult build(ServerPlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("a player must run the command");
        }

        ServerWorld world = player.getServerWorld();
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            throw new IllegalStateException(
                    "the province graybox can only be built in the Overworld"
            );
        }

        BlockPos origin = player.getBlockPos();
        int groundY = world.getTopY(
                Heightmap.Type.WORLD_SURFACE,
                origin.getX(),
                origin.getZ()
        ) - 1;

        requireFlatSurface(world, origin, groundY);

        buildRiver(world, origin, groundY);

        int routeUpdates = 0;
        for (Route route : ProvinceGrayboxLayout.routes()) {
            routeUpdates += drawRoute(world, origin, groundY, route);
        }

        int bridgeUpdates = drawBridge(world, origin, groundY);
        int landmarkUpdates = drawLandmarks(world, origin, groundY);

        return new BuildResult(
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                routeUpdates,
                bridgeUpdates,
                landmarkUpdates
        );
    }

    private static void requireFlatSurface(
            ServerWorld world,
            BlockPos origin,
            int groundY
    ) {
        for (Route route : ProvinceGrayboxLayout.routes()) {
            for (int i = 1; i < route.points().size(); i++) {
                Point from = route.points().get(i - 1);
                Point to = route.points().get(i);
                int steps = Math.max(
                        Math.abs(to.east() - from.east()),
                        Math.abs(to.south() - from.south())
                );
                int samples = Math.max(
                        1,
                        (steps + FLATNESS_SAMPLE_BLOCKS - 1)
                                / FLATNESS_SAMPLE_BLOCKS
                );

                for (int sample = 0; sample <= samples; sample++) {
                    double fraction = (double) sample / samples;
                    int x = origin.getX() + (int) Math.round(
                            from.east()
                                    + (to.east() - from.east()) * fraction
                    );
                    int z = origin.getZ() + (int) Math.round(
                            from.south()
                                    + (to.south() - from.south()) * fraction
                    );
                    int surfaceY = world.getTopY(
                            Heightmap.Type.WORLD_SURFACE,
                            x,
                            z
                    ) - 1;
                    if (surfaceY != groundY) {
                        throw new IllegalStateException(
                                "the route crosses uneven terrain near "
                                        + new BlockPos(x, surfaceY, z)
                                        + "; use a fresh Superflat Overworld"
                        );
                    }
                }
            }
        }
    }

    private static void buildRiver(
            ServerWorld world,
            BlockPos origin,
            int groundY
    ) {
        for (int east = RIVER_MIN_EAST; east <= RIVER_MAX_EAST; east++) {
            for (int south = RIVER_MIN_SOUTH;
                 south <= RIVER_MAX_SOUTH;
                 south++) {
                BlockPos bankSurface = at(origin, groundY, east, south);
                world.setBlockState(bankSurface, Blocks.AIR.getDefaultState(), 3);
                world.setBlockState(
                        bankSurface.down(),
                        Blocks.WATER.getDefaultState(),
                        3
                );
            }
        }
    }

    private static int drawRoute(
            ServerWorld world,
            BlockPos origin,
            int groundY,
            Route route
    ) {
        BlockState block = routeBlock(route.kind());
        int updates = 0;
        for (int i = 1; i < route.points().size(); i++) {
            Point from = route.points().get(i - 1);
            Point to = route.points().get(i);
            updates += drawSegment(
                    world,
                    origin,
                    groundY,
                    from,
                    to,
                    route.kind().width(),
                    block
            );
        }
        return updates;
    }

    private static int drawSegment(
            ServerWorld world,
            BlockPos origin,
            int groundY,
            Point from,
            Point to,
            int width,
            BlockState block
    ) {
        int x = from.east();
        int z = from.south();
        int targetX = to.east();
        int targetZ = to.south();
        int deltaX = Math.abs(targetX - x);
        int deltaZ = Math.abs(targetZ - z);
        int stepX = Integer.compare(targetX, x);
        int stepZ = Integer.compare(targetZ, z);
        int error = deltaX - deltaZ;
        int updates = 0;

        while (true) {
            int firstOffset = -(width / 2);
            for (int offset = firstOffset;
                 offset < firstOffset + width;
                 offset++) {
                int blockX = x + (deltaX >= deltaZ ? 0 : offset);
                int blockZ = z + (deltaX >= deltaZ ? offset : 0);
                world.setBlockState(
                        at(origin, groundY, blockX, blockZ),
                        block,
                        3
                );
                updates++;
            }

            if (x == targetX && z == targetZ) {
                break;
            }

            int doubledError = error * 2;
            if (doubledError > -deltaZ) {
                error -= deltaZ;
                x += stepX;
            }
            if (doubledError < deltaX) {
                error += deltaX;
                z += stepZ;
            }
        }

        return updates;
    }

    private static int drawBridge(
            ServerWorld world,
            BlockPos origin,
            int groundY
    ) {
        int updates = 0;
        for (int east = RIVER_MIN_EAST; east <= RIVER_MAX_EAST; east++) {
            for (int south = BRIDGE_MIN_SOUTH;
                 south <= BRIDGE_MAX_SOUTH;
                 south++) {
                world.setBlockState(
                        at(origin, groundY, east, south),
                        Blocks.OAK_PLANKS.getDefaultState(),
                        3
                );
                updates++;
            }
        }
        return updates;
    }

    private static int drawLandmarks(
            ServerWorld world,
            BlockPos origin,
            int groundY
    ) {
        int updates = 0;
        for (Landmark landmark : ProvinceGrayboxLayout.landmarks()) {
            BlockState block = markerBlock(landmark.node());
            int x = landmark.position().east() + landmark.markerEast();
            int z = landmark.position().south() + landmark.markerSouth();

            for (int east = -1; east <= 1; east++) {
                for (int south = -1; south <= 1; south++) {
                    world.setBlockState(
                            at(origin, groundY, x + east, z + south),
                            block,
                            3
                    );
                    updates++;
                }
            }

            for (int height = 1; height <= landmark.height(); height++) {
                world.setBlockState(
                        at(origin, groundY + height, x, z),
                        block,
                        3
                );
                updates++;
            }
        }
        return updates;
    }

    private static BlockState routeBlock(RouteKind kind) {
        return switch (kind) {
            case SAFE_ROAD -> Blocks.LIGHT_GRAY_CONCRETE.getDefaultState();
            case DANGEROUS_SHORTCUT -> Blocks.RED_CONCRETE.getDefaultState();
            case LEARNED_SHORTCUT -> Blocks.BROWN_CONCRETE.getDefaultState();
            case DETOUR -> Blocks.LIME_CONCRETE.getDefaultState();
            case OUTWARD -> Blocks.CYAN_CONCRETE.getDefaultState();
        };
    }

    private static BlockState markerBlock(Node node) {
        return switch (node) {
            case HOME -> Blocks.RED_CONCRETE.getDefaultState();
            case WILD -> Blocks.YELLOW_CONCRETE.getDefaultState();
            case CORRIDOR -> Blocks.ORANGE_CONCRETE.getDefaultState();
            case FORK -> Blocks.WHITE_CONCRETE.getDefaultState();
            case DANGER -> Blocks.BLACK_CONCRETE.getDefaultState();
            case REFUGE -> Blocks.LIGHT_BLUE_CONCRETE.getDefaultState();
            case REGIONAL_SETTLEMENT -> Blocks.PURPLE_CONCRETE.getDefaultState();
            case BEYOND -> Blocks.CYAN_CONCRETE.getDefaultState();
            case WORKLAND -> Blocks.LIME_CONCRETE.getDefaultState();
            case LOCAL_RUIN -> Blocks.GREEN_CONCRETE.getDefaultState();
            case UNFINISHED_SITE -> Blocks.MAGENTA_CONCRETE.getDefaultState();
        };
    }

    private static BlockPos at(
            BlockPos origin,
            int groundY,
            int east,
            int south
    ) {
        return new BlockPos(
                origin.getX() + east,
                groundY,
                origin.getZ() + south
        );
    }

    public record BuildResult(
            int originX,
            int originY,
            int originZ,
            int routeBlockUpdates,
            int bridgeBlockUpdates,
            int landmarkBlockUpdates
    ) {
        public String summary() {
            return "province graybox built at origin "
                    + originX + "," + originY + "," + originZ
                    + " (+X east, +Z south); routes=" + routeBlockUpdates
                    + ", bridge=" + bridgeBlockUpdates
                    + ", landmarks=" + landmarkBlockUpdates
                    + " | routes: light-gray=safe, red=danger shortcut, "
                    + "brown=learned return, lime=detours, cyan=outward"
                    + " | markers: red=A, yellow=B, orange=C, white=D, "
                    + "black=E, light-blue=F, purple=G, cyan=H, "
                    + "lime=B2, green=R1, magenta=R2";
        }
    }
}
