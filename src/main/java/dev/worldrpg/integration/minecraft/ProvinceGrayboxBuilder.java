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
        int groundY = loadedSurfaceY(
                world,
                origin.getX(),
                origin.getZ()
        );

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
                    int surfaceY = loadedSurfaceY(
                            world,
                            x,
                            z
                    );
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


    public static PlaceBuildResult buildReadablePlaces(
            ServerWorld world,
            int originX,
            int originZ
    ) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            throw new IllegalStateException(
                    "province graybox places can only be built in the Overworld"
            );
        }

        int groundY = loadedSurfaceY(
                world,
                originX,
                originZ
        );

        int cleared = 0;
        int paths = 0;
        int structures = 0;

        Point home = ProvinceGrayboxLayout.nodePosition(Node.HOME);
        Point refuge = ProvinceGrayboxLayout.nodePosition(Node.REFUGE);
        Point regional = ProvinceGrayboxLayout.nodePosition(
                Node.REGIONAL_SETTLEMENT
        );
        Point fork = ProvinceGrayboxLayout.nodePosition(Node.FORK);
        Point danger = ProvinceGrayboxLayout.nodePosition(Node.DANGER);
        Point ruin = ProvinceGrayboxLayout.nodePosition(Node.LOCAL_RUIN);
        Point workland = ProvinceGrayboxLayout.nodePosition(Node.WORKLAND);
        Point beyond = ProvinceGrayboxLayout.nodePosition(Node.BEYOND);

        cleared += clearVolume(
                world,
                originX + home.east() - 52,
                originX + home.east() + 52,
                groundY + 1,
                groundY + 14,
                originZ + home.south() - 42,
                originZ + home.south() + 42
        );
        paths += buildHomeSettlement(
                world,
                originX,
                originZ,
                groundY,
                home
        );
        structures += LAST_STRUCTURE_WRITES;

        cleared += clearVolume(
                world,
                originX + refuge.east() - 30,
                originX + refuge.east() + 30,
                groundY + 1,
                groundY + 12,
                originZ + refuge.south() - 26,
                originZ + refuge.south() + 26
        );
        paths += buildRefugePlace(
                world,
                originX,
                originZ,
                groundY,
                refuge
        );
        structures += LAST_STRUCTURE_WRITES;

        cleared += clearVolume(
                world,
                originX + regional.east() - 76,
                originX + regional.east() + 76,
                groundY + 1,
                groundY + 18,
                originZ + regional.south() - 66,
                originZ + regional.south() + 66
        );
        paths += buildRegionalSettlement(
                world,
                originX,
                originZ,
                groundY,
                regional
        );
        structures += LAST_STRUCTURE_WRITES;

        structures += buildForkLandmark(
                world,
                originX,
                originZ,
                groundY,
                fork
        );
        structures += buildDangerRuin(
                world,
                originX,
                originZ,
                groundY,
                danger
        );
        structures += buildLocalRuin(
                world,
                originX,
                originZ,
                groundY,
                ruin
        );
        structures += buildWorkland(
                world,
                originX,
                originZ,
                groundY,
                workland
        );
        structures += buildBeyondGate(
                world,
                originX,
                originZ,
                groundY,
                beyond
        );

        return new PlaceBuildResult(
                cleared,
                paths,
                structures
        );
    }

    /*
     * The following graybox structures are intentionally cheap, readable and
     * over-scaled. They are not production architecture. Their job is to let
     * a human tester understand "home", "refuge", "regional town", "ruin",
     * and "danger site" without translating colored poles in their head.
     */
    private static int LAST_STRUCTURE_WRITES = 0;

    private static int buildHomeSettlement(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point home
    ) {
        int cx = originX + home.east();
        int cz = originZ + home.south();
        int paths = 0;
        int structures = 0;

        paths += surfacePath(
                world,
                groundY,
                cx - 50,
                cz,
                cx + 50,
                cz,
                5,
                Blocks.GRAVEL.getDefaultState()
        );
        paths += surfacePath(
                world,
                groundY,
                cx,
                cz - 34,
                cx,
                cz + 34,
                3,
                Blocks.COARSE_DIRT.getDefaultState()
        );
        paths += ringPath(
                world,
                groundY,
                cx,
                cz,
                23,
                17,
                Blocks.COARSE_DIRT.getDefaultState()
        );

        structures += buildOpenHouse(
                world, groundY, cx - 30, cz - 19,
                13, 10, 5,
                Blocks.SPRUCE_PLANKS.getDefaultState(),
                Blocks.STRIPPED_SPRUCE_LOG.getDefaultState(),
                DirectionFacing.EAST
        );
        structures += buildOpenHouse(
                world, groundY, cx - 28, cz + 18,
                11, 9, 5,
                Blocks.OAK_PLANKS.getDefaultState(),
                Blocks.STRIPPED_OAK_LOG.getDefaultState(),
                DirectionFacing.EAST
        );
        structures += buildOpenHouse(
                world, groundY, cx + 29, cz - 18,
                12, 9, 5,
                Blocks.SPRUCE_PLANKS.getDefaultState(),
                Blocks.STRIPPED_SPRUCE_LOG.getDefaultState(),
                DirectionFacing.WEST
        );
        structures += buildOpenHouse(
                world, groundY, cx + 31, cz + 18,
                12, 10, 5,
                Blocks.OAK_PLANKS.getDefaultState(),
                Blocks.STRIPPED_OAK_LOG.getDefaultState(),
                DirectionFacing.WEST
        );

        // Deliberately larger inn / support building.
        structures += buildOpenHouse(
                world, groundY, cx - 3, cz - 27,
                19, 12, 6,
                Blocks.DARK_OAK_PLANKS.getDefaultState(),
                Blocks.STRIPPED_DARK_OAK_LOG.getDefaultState(),
                DirectionFacing.SOUTH
        );

        structures += buildWell(
                world,
                groundY,
                cx,
                cz + 3
        );
        structures += buildGatePair(
                world,
                groundY,
                cx + 45,
                cz,
                Blocks.STONE_BRICKS.getDefaultState()
        );

        LAST_STRUCTURE_WRITES = structures;
        return paths;
    }

    private static int buildRefugePlace(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point refuge
    ) {
        int cx = originX + refuge.east();
        int cz = originZ + refuge.south();
        int paths = 0;
        int structures = 0;

        paths += surfacePath(
                world,
                groundY,
                cx - 28,
                cz,
                cx + 28,
                cz,
                5,
                Blocks.GRAVEL.getDefaultState()
        );
        paths += surfacePath(
                world,
                groundY,
                cx,
                cz - 20,
                cx,
                cz + 20,
                3,
                Blocks.COARSE_DIRT.getDefaultState()
        );

        structures += buildFenceRect(
                world,
                groundY,
                cx - 23,
                cz - 18,
                cx + 23,
                cz + 18,
                Blocks.SPRUCE_FENCE.getDefaultState(),
                cx - 23,
                cz,
                cx + 23,
                cz
        );

        structures += buildOpenHouse(
                world, groundY, cx - 11, cz - 10,
                16, 10, 5,
                Blocks.SPRUCE_PLANKS.getDefaultState(),
                Blocks.STRIPPED_SPRUCE_LOG.getDefaultState(),
                DirectionFacing.SOUTH
        );
        structures += buildOpenHouse(
                world, groundY, cx + 12, cz + 10,
                10, 8, 4,
                Blocks.COBBLESTONE.getDefaultState(),
                Blocks.OAK_LOG.getDefaultState(),
                DirectionFacing.NORTH
        );
        structures += buildWatchTower(
                world,
                groundY,
                cx + 18,
                cz - 13,
                9,
                Blocks.COBBLESTONE.getDefaultState()
        );
        structures += buildCamp(
                world,
                groundY,
                cx,
                cz + 8
        );

        LAST_STRUCTURE_WRITES = structures;
        return paths;
    }

    private static int buildRegionalSettlement(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point regional
    ) {
        int cx = originX + regional.east();
        int cz = originZ + regional.south();
        int paths = 0;
        int structures = 0;

        // The north-south avenue continues the approach from D.
        paths += surfacePath(
                world,
                groundY,
                cx,
                cz - 64,
                cx,
                cz + 64,
                7,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        paths += surfacePath(
                world,
                groundY,
                cx - 68,
                cz,
                cx + 68,
                cz,
                5,
                Blocks.GRAVEL.getDefaultState()
        );
        paths += surfacePath(
                world,
                groundY,
                cx - 48,
                cz - 32,
                cx + 48,
                cz - 32,
                3,
                Blocks.COARSE_DIRT.getDefaultState()
        );
        paths += surfacePath(
                world,
                groundY,
                cx - 48,
                cz + 32,
                cx + 48,
                cz + 32,
                3,
                Blocks.COARSE_DIRT.getDefaultState()
        );

        structures += buildWallRectWithGates(
                world,
                groundY,
                cx - 70,
                cz - 60,
                cx + 70,
                cz + 60,
                3,
                Blocks.STONE_BRICKS.getDefaultState()
        );

        // Central plaza / market identity.
        structures += fillSurfaceRect(
                world,
                groundY,
                cx - 12,
                cz - 10,
                cx + 12,
                cz + 10,
                Blocks.SMOOTH_STONE.getDefaultState()
        );
        structures += buildWell(
                world,
                groundY,
                cx,
                cz
        );

        int[][] buildings = new int[][]{
                {-45, -43, 18, 11, 6},
                {-22, -43, 14, 10, 5},
                {22, -43, 15, 10, 5},
                {46, -43, 17, 11, 6},
                {-45, -15, 16, 10, 5},
                {45, -15, 16, 10, 5},
                {-46, 19, 18, 11, 5},
                {46, 19, 18, 11, 5},
                {-43, 45, 20, 12, 6},
                {-15, 45, 14, 10, 5},
                {17, 45, 14, 10, 5},
                {45, 45, 19, 12, 6}
        };

        for (int i = 0; i < buildings.length; i++) {
            int[] b = buildings[i];
            BlockState wall = i % 3 == 0
                    ? Blocks.DARK_OAK_PLANKS.getDefaultState()
                    : i % 3 == 1
                    ? Blocks.SPRUCE_PLANKS.getDefaultState()
                    : Blocks.STONE_BRICKS.getDefaultState();
            BlockState frame = i % 2 == 0
                    ? Blocks.STRIPPED_DARK_OAK_LOG.getDefaultState()
                    : Blocks.STRIPPED_SPRUCE_LOG.getDefaultState();
            DirectionFacing facing = b[1] < 0
                    ? DirectionFacing.SOUTH
                    : DirectionFacing.NORTH;
            structures += buildOpenHouse(
                    world,
                    groundY,
                    cx + b[0],
                    cz + b[1],
                    b[2],
                    b[3],
                    b[4],
                    wall,
                    frame,
                    facing
            );
        }

        // A visually dominant civic/inn hall gives the town an actual center.
        structures += buildOpenHouse(
                world,
                groundY,
                cx + 1,
                cz + 34,
                25,
                15,
                8,
                Blocks.STONE_BRICKS.getDefaultState(),
                Blocks.STRIPPED_DARK_OAK_LOG.getDefaultState(),
                DirectionFacing.NORTH
        );

        structures += buildWatchTower(
                world,
                groundY,
                cx - 61,
                cz - 51,
                12,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        structures += buildWatchTower(
                world,
                groundY,
                cx + 61,
                cz - 51,
                12,
                Blocks.STONE_BRICKS.getDefaultState()
        );

        LAST_STRUCTURE_WRITES = structures;
        return paths;
    }

    private static int buildForkLandmark(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point fork
    ) {
        int cx = originX + fork.east();
        int cz = originZ + fork.south();
        int changed = 0;

        changed += fillSurfaceRect(
                world,
                groundY,
                cx - 8,
                cz - 8,
                cx + 8,
                cz + 8,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        changed += buildWatchTower(
                world,
                groundY,
                cx + 11,
                cz + 10,
                10,
                Blocks.COBBLESTONE.getDefaultState()
        );
        changed += buildGatePair(
                world,
                groundY,
                cx,
                cz - 7,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        return changed;
    }

    private static int buildDangerRuin(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point danger
    ) {
        int cx = originX + danger.east();
        int cz = originZ + danger.south();
        int changed = 0;

        changed += clearVolume(
                world,
                cx - 30,
                cx + 30,
                groundY + 1,
                groundY + 18,
                cz - 26,
                cz + 26
        );
        changed += buildBrokenWallRect(
                world,
                groundY,
                cx - 25,
                cz - 20,
                cx + 25,
                cz + 20,
                7,
                Blocks.DEEPSLATE_BRICKS.getDefaultState()
        );
        changed += buildWatchTower(
                world,
                groundY,
                cx - 19,
                cz - 14,
                16,
                Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState()
        );
        changed += buildWatchTower(
                world,
                groundY,
                cx + 20,
                cz + 13,
                13,
                Blocks.DEEPSLATE_BRICKS.getDefaultState()
        );
        changed += fillSurfaceRect(
                world,
                groundY,
                cx - 9,
                cz - 8,
                cx + 9,
                cz + 8,
                Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
        );
        return changed;
    }

    private static int buildLocalRuin(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point ruin
    ) {
        int cx = originX + ruin.east();
        int cz = originZ + ruin.south();
        int changed = 0;
        changed += buildBrokenWallRect(
                world,
                groundY,
                cx - 15,
                cz - 12,
                cx + 15,
                cz + 12,
                4,
                Blocks.MOSSY_STONE_BRICKS.getDefaultState()
        );
        changed += buildOpenHouse(
                world,
                groundY,
                cx + 2,
                cz + 1,
                12,
                9,
                4,
                Blocks.MOSSY_COBBLESTONE.getDefaultState(),
                Blocks.CRACKED_STONE_BRICKS.getDefaultState(),
                DirectionFacing.NORTH
        );
        return changed;
    }

    private static int buildWorkland(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point workland
    ) {
        int cx = originX + workland.east();
        int cz = originZ + workland.south();
        int changed = 0;

        changed += buildFenceRect(
                world,
                groundY,
                cx - 25,
                cz - 18,
                cx + 25,
                cz + 18,
                Blocks.OAK_FENCE.getDefaultState(),
                cx - 25,
                cz,
                cx + 25,
                cz
        );
        changed += fillSurfaceRect(
                world,
                groundY,
                cx - 20,
                cz - 13,
                cx - 3,
                cz + 13,
                Blocks.FARMLAND.getDefaultState()
        );
        changed += fillSurfaceRect(
                world,
                groundY,
                cx + 4,
                cz - 13,
                cx + 20,
                cz + 13,
                Blocks.COARSE_DIRT.getDefaultState()
        );
        changed += buildOpenHouse(
                world,
                groundY,
                cx + 13,
                cz,
                11,
                9,
                4,
                Blocks.OAK_PLANKS.getDefaultState(),
                Blocks.STRIPPED_OAK_LOG.getDefaultState(),
                DirectionFacing.WEST
        );
        return changed;
    }

    private static int buildBeyondGate(
            ServerWorld world,
            int originX,
            int originZ,
            int groundY,
            Point beyond
    ) {
        int cx = originX + beyond.east();
        int cz = originZ + beyond.south();
        int changed = 0;
        changed += buildGatePair(
                world,
                groundY,
                cx,
                cz,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        changed += buildWatchTower(
                world,
                groundY,
                cx,
                cz + 12,
                11,
                Blocks.STONE_BRICKS.getDefaultState()
        );
        return changed;
    }

    private static int surfacePath(
            ServerWorld world,
            int groundY,
            int x1,
            int z1,
            int x2,
            int z2,
            int width,
            BlockState state
    ) {
        return drawAbsoluteSegment(
                world,
                groundY,
                x1,
                z1,
                x2,
                z2,
                width,
                state
        );
    }

    private static int ringPath(
            ServerWorld world,
            int groundY,
            int cx,
            int cz,
            int radiusX,
            int radiusZ,
            BlockState state
    ) {
        int changed = 0;
        changed += fillSurfaceRect(
                world, groundY,
                cx - radiusX, cz - radiusZ,
                cx + radiusX, cz - radiusZ + 2,
                state
        );
        changed += fillSurfaceRect(
                world, groundY,
                cx - radiusX, cz + radiusZ - 2,
                cx + radiusX, cz + radiusZ,
                state
        );
        changed += fillSurfaceRect(
                world, groundY,
                cx - radiusX, cz - radiusZ,
                cx - radiusX + 2, cz + radiusZ,
                state
        );
        changed += fillSurfaceRect(
                world, groundY,
                cx + radiusX - 2, cz - radiusZ,
                cx + radiusX, cz + radiusZ,
                state
        );
        return changed;
    }

    private static int drawAbsoluteSegment(
            ServerWorld world,
            int groundY,
            int x1,
            int z1,
            int x2,
            int z2,
            int width,
            BlockState state
    ) {
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = Integer.compare(x2, x1);
        int sz = Integer.compare(z2, z1);
        int error = dx - dz;
        int x = x1;
        int z = z1;
        int changed = 0;

        while (true) {
            int firstOffset = -(width / 2);
            for (int offset = firstOffset;
                 offset < firstOffset + width;
                 offset++) {
                int bx = x + (dx >= dz ? 0 : offset);
                int bz = z + (dx >= dz ? offset : 0);
                world.setBlockState(
                        new BlockPos(bx, groundY, bz),
                        state,
                        3
                );
                changed++;
            }

            if (x == x2 && z == z2) {
                break;
            }

            int doubled = error * 2;
            if (doubled > -dz) {
                error -= dz;
                x += sx;
            }
            if (doubled < dx) {
                error += dx;
                z += sz;
            }
        }
        return changed;
    }

    private static int clearVolume(
            ServerWorld world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
        int changed = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getChunk(
                        Math.floorDiv(x, 16),
                        Math.floorDiv(z, 16)
                );
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlockState(
                                pos,
                                Blocks.AIR.getDefaultState(),
                                3
                        );
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    private static int fillSurfaceRect(
            ServerWorld world,
            int groundY,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            BlockState state
    ) {
        int changed = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.setBlockState(
                        new BlockPos(x, groundY, z),
                        state,
                        3
                );
                changed++;
            }
        }
        return changed;
    }

    private enum DirectionFacing {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private static int buildOpenHouse(
            ServerWorld world,
            int groundY,
            int cx,
            int cz,
            int width,
            int depth,
            int height,
            BlockState wall,
            BlockState frame,
            DirectionFacing facing
    ) {
        int changed = 0;
        int minX = cx - width / 2;
        int maxX = cx + width / 2;
        int minZ = cz - depth / 2;
        int maxZ = cz + depth / 2;

        changed += fillSurfaceRect(
                world,
                groundY,
                minX,
                minZ,
                maxX,
                maxZ,
                Blocks.SMOOTH_STONE.getDefaultState()
        );

        for (int y = 1; y <= height; y++) {
            for (int x = minX; x <= maxX; x++) {
                changed += setIfHouseWall(
                        world, groundY, x, minZ, y,
                        minX, maxX, minZ, maxZ,
                        facing, wall, frame
                );
                changed += setIfHouseWall(
                        world, groundY, x, maxZ, y,
                        minX, maxX, minZ, maxZ,
                        facing, wall, frame
                );
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                changed += setIfHouseWall(
                        world, groundY, minX, z, y,
                        minX, maxX, minZ, maxZ,
                        facing, wall, frame
                );
                changed += setIfHouseWall(
                        world, groundY, maxX, z, y,
                        minX, maxX, minZ, maxZ,
                        facing, wall, frame
                );
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.setBlockState(
                        new BlockPos(
                                x,
                                groundY + height + 1,
                                z
                        ),
                        Blocks.SPRUCE_SLAB.getDefaultState(),
                        3
                );
                changed++;
            }
        }
        return changed;
    }

    private static int setIfHouseWall(
            ServerWorld world,
            int groundY,
            int x,
            int z,
            int y,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            DirectionFacing facing,
            BlockState wall,
            BlockState frame
    ) {
        boolean doorway = switch (facing) {
            case NORTH -> z == minZ
                    && Math.abs(x - ((minX + maxX) / 2)) <= 1
                    && y <= 3;
            case SOUTH -> z == maxZ
                    && Math.abs(x - ((minX + maxX) / 2)) <= 1
                    && y <= 3;
            case EAST -> x == maxX
                    && Math.abs(z - ((minZ + maxZ) / 2)) <= 1
                    && y <= 3;
            case WEST -> x == minX
                    && Math.abs(z - ((minZ + maxZ) / 2)) <= 1
                    && y <= 3;
        };
        if (doorway) {
            return 0;
        }

        boolean corner =
                (x == minX || x == maxX)
                        && (z == minZ || z == maxZ);
        world.setBlockState(
                new BlockPos(x, groundY + y, z),
                corner ? frame : wall,
                3
        );
        return 1;
    }

    private static int buildWell(
            ServerWorld world,
            int groundY,
            int cx,
            int cz
    ) {
        int changed = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean edge =
                        Math.abs(x) == 2 || Math.abs(z) == 2;
                if (edge) {
                    world.setBlockState(
                            new BlockPos(
                                    cx + x,
                                    groundY + 1,
                                    cz + z
                            ),
                            Blocks.COBBLESTONE.getDefaultState(),
                            3
                    );
                    changed++;
                }
            }
        }
        world.setBlockState(
                new BlockPos(cx, groundY + 1, cz),
                Blocks.WATER.getDefaultState(),
                3
        );
        changed++;
        return changed;
    }

    private static int buildGatePair(
            ServerWorld world,
            int groundY,
            int cx,
            int cz,
            BlockState state
    ) {
        int changed = 0;
        for (int side : new int[]{-4, 4}) {
            for (int y = 1; y <= 6; y++) {
                world.setBlockState(
                        new BlockPos(
                                cx,
                                groundY + y,
                                cz + side
                        ),
                        state,
                        3
                );
                changed++;
            }
        }
        for (int z = -4; z <= 4; z++) {
            world.setBlockState(
                    new BlockPos(
                            cx,
                            groundY + 6,
                            cz + z
                    ),
                    state,
                    3
            );
            changed++;
        }
        return changed;
    }

    private static int buildWatchTower(
            ServerWorld world,
            int groundY,
            int cx,
            int cz,
            int height,
            BlockState state
    ) {
        int changed = 0;
        for (int y = 1; y <= height; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        world.setBlockState(
                                new BlockPos(
                                        cx + x,
                                        groundY + y,
                                        cz + z
                                ),
                                state,
                                3
                        );
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    private static int buildFenceRect(
            ServerWorld world,
            int groundY,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            BlockState state,
            int gate1X,
            int gate1Z,
            int gate2X,
            int gate2Z
    ) {
        int changed = 0;
        for (int x = minX; x <= maxX; x++) {
            if (!(x == gate1X && minZ == gate1Z)
                    && !(x == gate2X && minZ == gate2Z)) {
                world.setBlockState(
                        new BlockPos(x, groundY + 1, minZ),
                        state,
                        3
                );
                changed++;
            }
            if (!(x == gate1X && maxZ == gate1Z)
                    && !(x == gate2X && maxZ == gate2Z)) {
                world.setBlockState(
                        new BlockPos(x, groundY + 1, maxZ),
                        state,
                        3
                );
                changed++;
            }
        }
        for (int z = minZ + 1; z < maxZ; z++) {
            if (!(minX == gate1X && z == gate1Z)
                    && !(minX == gate2X && z == gate2Z)) {
                world.setBlockState(
                        new BlockPos(minX, groundY + 1, z),
                        state,
                        3
                );
                changed++;
            }
            if (!(maxX == gate1X && z == gate1Z)
                    && !(maxX == gate2X && z == gate2Z)) {
                world.setBlockState(
                        new BlockPos(maxX, groundY + 1, z),
                        state,
                        3
                );
                changed++;
            }
        }
        return changed;
    }

    private static int buildWallRectWithGates(
            ServerWorld world,
            int groundY,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            int height,
            BlockState state
    ) {
        int changed = 0;
        for (int y = 1; y <= height; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (Math.abs(x - ((minX + maxX) / 2)) > 4) {
                    world.setBlockState(
                            new BlockPos(x, groundY + y, minZ),
                            state,
                            3
                    );
                    world.setBlockState(
                            new BlockPos(x, groundY + y, maxZ),
                            state,
                            3
                    );
                    changed += 2;
                }
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                if (Math.abs(z - ((minZ + maxZ) / 2)) > 4) {
                    world.setBlockState(
                            new BlockPos(minX, groundY + y, z),
                            state,
                            3
                    );
                    world.setBlockState(
                            new BlockPos(maxX, groundY + y, z),
                            state,
                            3
                    );
                    changed += 2;
                }
            }
        }
        return changed;
    }

    private static int buildBrokenWallRect(
            ServerWorld world,
            int groundY,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            int height,
            BlockState state
    ) {
        int changed = 0;
        for (int y = 1; y <= height; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (((x + y) & 5) != 0) {
                    world.setBlockState(
                            new BlockPos(x, groundY + y, minZ),
                            state,
                            3
                    );
                    changed++;
                }
                if (((x - y) & 6) != 0) {
                    world.setBlockState(
                            new BlockPos(x, groundY + y, maxZ),
                            state,
                            3
                    );
                    changed++;
                }
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                if (((z + y) & 5) != 0) {
                    world.setBlockState(
                            new BlockPos(minX, groundY + y, z),
                            state,
                            3
                    );
                    changed++;
                }
                if (((z - y) & 6) != 0) {
                    world.setBlockState(
                            new BlockPos(maxX, groundY + y, z),
                            state,
                            3
                    );
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int buildCamp(
            ServerWorld world,
            int groundY,
            int cx,
            int cz
    ) {
        int changed = 0;
        world.setBlockState(
                new BlockPos(cx, groundY + 1, cz),
                Blocks.CAMPFIRE.getDefaultState(),
                3
        );
        changed++;
        for (int[] p : new int[][]{
                {-2, 0},
                {2, 0},
                {0, -2},
                {0, 2}
        }) {
            world.setBlockState(
                    new BlockPos(
                            cx + p[0],
                            groundY + 1,
                            cz + p[1]
                    ),
                    Blocks.OAK_STAIRS.getDefaultState(),
                    3
            );
            changed++;
        }
        return changed;
    }

    public record PlaceBuildResult(
            int clearedBlockUpdates,
            int pathBlockUpdates,
            int structureBlockUpdates
    ) {
        public String summary() {
            return "province readable places built"
                    + " | cleared=" + clearedBlockUpdates
                    + " paths=" + pathBlockUpdates
                    + " structures=" + structureBlockUpdates
                    + " | authored: Home A, Refuge F, Regional G, Fork D, Danger E, Local Ruin R1, Workland B2, Beyond H";
        }
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

    private static int loadedSurfaceY(
            ServerWorld world,
            int x,
            int z
    ) {
        /*
         * getTopY() can report the dimension floor for distant, not-yet-loaded
         * chunks. The topology spans nearly ten thousand blocks, so the
         * flatness preflight must explicitly load/generate the sampled chunk
         * before reading its heightmap. Otherwise a perfectly flat Superflat
         * world is falsely rejected as "uneven" around y=-65.
         */
        world.getChunk(
                Math.floorDiv(x, 16),
                Math.floorDiv(z, 16)
        );
        return world.getTopY(
                Heightmap.Type.WORLD_SURFACE,
                x,
                z
        ) - 1;
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
