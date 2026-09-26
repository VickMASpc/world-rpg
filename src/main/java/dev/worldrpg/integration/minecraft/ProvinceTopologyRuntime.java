package dev.worldrpg.integration.minecraft;

import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Journey;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Node;
import dev.worldrpg.integration.minecraft.ProvinceGrayboxLayout.Point;
import dev.worldrpg.persistence.fabric.WorldRpgPersistentState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistent/instrumented physical proof runtime for the disposable
 * first-province topology graybox.
 *
 * <p>This deliberately does not become production geography. It exists to
 * measure the constitutional questions of distance, route choice, landmark
 * memory and first/known journey differences before expensive terrain work.</p>
 */
public final class ProvinceTopologyRuntime {
    private static final String ROOT = "province_topology_graybox";
    private static final String ORIGIN_X = "origin_x";
    private static final String ORIGIN_Z = "origin_z";
    private static final String BUILT = "built";
    private static final String MEASUREMENTS = "measurements";
    private static final String ELAPSED_TICKS = "elapsed_ticks";
    private static final String PATH_BLOCKS = "path_blocks";
    private static final String MODE = "mode";
    private static final String RUNS = "runs";

    private static final double START_TOLERANCE_BLOCKS = 28.0;
    private static final double END_TOLERANCE_BLOCKS = 36.0;

    private final Map<UUID, ActiveMeasurement> active =
            new LinkedHashMap<>();

    private MinecraftServer server;

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        active.clear();
    }

    public void stop() {
        active.clear();
        server = null;
    }

    public ProvinceGrayboxBuilder.BuildResult build(
            ServerPlayerEntity player
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        if (loadState().isPresent()) {
            throw new IllegalStateException(
                    "province topology graybox already has a persisted origin in this world; use a fresh Superflat world for a new topology run"
            );
        }

        ProvinceGrayboxBuilder.BuildResult result =
                ProvinceGrayboxBuilder.build(player);

        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData = persistence.readWorldData();
        NbtCompound root = new NbtCompound();
        root.putBoolean(BUILT, true);
        root.putInt(ORIGIN_X, result.originX());
        root.putInt(ORIGIN_Z, result.originZ());
        root.put(MEASUREMENTS, new NbtCompound());
        worldData.put(ROOT, root);
        persistence.writeWorldData(worldData);

        return result;
    }

    public Optional<GrayboxState> state() {
        requireStarted();
        return loadState();
    }

    public String status(ServerPlayerEntity player) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        GrayboxState state = loadState().orElseThrow(() ->
                new IllegalStateException(
                        "province topology graybox is not built in this world"
                )
        );

        Node nearest = nearestNode(player, state);
        Point nearestPoint =
                ProvinceGrayboxLayout.nodePosition(nearest);
        double nearestDistance = horizontalDistance(
                player,
                state.absoluteX(nearestPoint),
                state.absoluteZ(nearestPoint)
        );

        ActiveMeasurement measurement =
                active.get(player.getUuid());

        return "province topology | origin="
                + state.originX() + "," + state.originZ()
                + " nearest=" + nearest
                + " distance="
                + format(nearestDistance)
                + " blocks"
                + " active="
                + (measurement == null
                ? "none"
                : measurement.journeyId()
                + "/"
                + measurement.mode().name()
                        .toLowerCase(Locale.ROOT));
    }

    public List<String> nodeLines(
            ServerPlayerEntity player
    ) {
        GrayboxState state = requireState();
        List<String> lines = new ArrayList<>();

        for (Node node : Node.values()) {
            Point point =
                    ProvinceGrayboxLayout.nodePosition(node);
            int x = state.absoluteX(point);
            int z = state.absoluteZ(point);
            double distance =
                    horizontalDistance(player, x, z);
            lines.add(
                    node
                            + " = "
                            + x
                            + ","
                            + surfaceY(player.getServerWorld(), x, z)
                            + ","
                            + z
                            + " | fromYou="
                            + format(distance)
                            + " blocks"
            );
        }

        return List.copyOf(lines);
    }

    public List<String> journeyLines() {
        requireStarted();
        List<String> lines = new ArrayList<>();

        for (Journey journey :
                ProvinceGrayboxLayout.journeys()) {
            String target = journey.hasTargetBand()
                    ? format(journey.targetMinMinutes())
                    + "-"
                    + format(journey.targetMaxMinutes())
                    + " min"
                    : "relative target";
            lines.add(
                    journey.id()
                            + " | "
                            + journey.start()
                            + " -> "
                            + journey.end()
                            + " | path≈"
                            + Math.round(journey.pathBlocks())
                            + " blocks"
                            + " | target="
                            + target
                            + " | "
                            + journey.intent()
            );
        }

        return List.copyOf(lines);
    }

    public StartResult startMeasurement(
            ServerPlayerEntity player,
            String journeyId,
            MeasurementMode mode
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(mode, "mode");

        GrayboxState state = requireState();
        Journey journey =
                ProvinceGrayboxLayout.journey(journeyId);

        if (!player.getServerWorld()
                .getRegistryKey()
                .equals(World.OVERWORLD)) {
            throw new IllegalStateException(
                    "province topology measurements must run in the Overworld graybox"
            );
        }

        Point start =
                ProvinceGrayboxLayout.nodePosition(
                        journey.start()
                );
        int startX = state.absoluteX(start);
        int startZ = state.absoluteZ(start);
        double distance =
                horizontalDistance(
                        player,
                        startX,
                        startZ
                );

        if (distance > START_TOLERANCE_BLOCKS) {
            throw new IllegalStateException(
                    "stand at "
                            + journey.start()
                            + " before starting "
                            + journey.id()
                            + " (you are "
                            + format(distance)
                            + " blocks away; tolerance "
                            + format(START_TOLERANCE_BLOCKS)
                            + ")"
            );
        }

        ActiveMeasurement measurement =
                new ActiveMeasurement(
                        journey.id(),
                        mode,
                        server.getTicks(),
                        player.getX(),
                        player.getZ()
                );
        active.put(
                player.getUuid(),
                measurement
        );

        return new StartResult(
                journey,
                mode,
                startX,
                startZ
        );
    }

    public FinishResult finishMeasurement(
            ServerPlayerEntity player
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");

        ActiveMeasurement measurement =
                active.get(player.getUuid());
        if (measurement == null) {
            throw new IllegalStateException(
                    "no province topology measurement is active"
            );
        }

        GrayboxState state = requireState();
        Journey journey =
                ProvinceGrayboxLayout.journey(
                        measurement.journeyId()
                );
        Point end =
                ProvinceGrayboxLayout.nodePosition(
                        journey.end()
                );
        int endX = state.absoluteX(end);
        int endZ = state.absoluteZ(end);
        double distance =
                horizontalDistance(
                        player,
                        endX,
                        endZ
                );

        if (distance > END_TOLERANCE_BLOCKS) {
            throw new IllegalStateException(
                    "reach "
                            + journey.end()
                            + " before finishing "
                            + journey.id()
                            + " (you are "
                            + format(distance)
                            + " blocks away; tolerance "
                            + format(END_TOLERANCE_BLOCKS)
                            + ")"
            );
        }

        long elapsedTicks = Math.max(
                1L,
                server.getTicks()
                        - measurement.startedAtTick()
        );
        double elapsedMinutes =
                elapsedTicks / 1200.0;

        MeasurementRecord record =
                persistMeasurement(
                        journey,
                        measurement.mode(),
                        elapsedTicks
                );
        active.remove(player.getUuid());

        Optional<MeasurementRecord> comparison =
                comparisonFor(
                        journey,
                        measurement.mode()
                );

        return new FinishResult(
                journey,
                measurement.mode(),
                elapsedTicks,
                elapsedMinutes,
                record.runCount(),
                comparison
        );
    }

    public boolean cancelMeasurement(
            ServerPlayerEntity player
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        return active.remove(player.getUuid()) != null;
    }

    public List<String> historyLines() {
        requireStarted();
        NbtCompound measurements =
                measurementRoot();
        List<String> lines = new ArrayList<>();

        for (String key : measurements.getKeys()
                .stream()
                .sorted()
                .toList()) {
            if (!measurements.contains(
                    key,
                    NbtElement.COMPOUND_TYPE
            )) {
                continue;
            }

            NbtCompound entry =
                    measurements.getCompound(key);
            long ticks =
                    entry.getLong(ELAPSED_TICKS);
            int runs =
                    entry.getInt(RUNS);
            lines.add(
                    key
                            + " | latest="
                            + format(ticks / 1200.0)
                            + " min"
                            + " | runs="
                            + runs
            );
        }

        if (lines.isEmpty()) {
            lines.add("No province topology measurements recorded yet.");
        }
        return List.copyOf(lines);
    }

    private MeasurementRecord persistMeasurement(
            Journey journey,
            MeasurementMode mode,
            long elapsedTicks
    ) {
        WorldRpgPersistentState persistence =
                WorldRpgPersistentState.get(server);
        NbtCompound worldData =
                persistence.readWorldData();
        NbtCompound root =
                worldData.getCompound(ROOT);
        NbtCompound measurements =
                root.contains(
                        MEASUREMENTS,
                        NbtElement.COMPOUND_TYPE
                )
                        ? root.getCompound(MEASUREMENTS)
                        : new NbtCompound();

        String key =
                measurementKey(
                        journey.id(),
                        mode
                );
        int previousRuns = 0;
        if (measurements.contains(
                key,
                NbtElement.COMPOUND_TYPE
        )) {
            previousRuns =
                    measurements.getCompound(key)
                            .getInt(RUNS);
        }

        NbtCompound entry =
                new NbtCompound();
        entry.putLong(
                ELAPSED_TICKS,
                elapsedTicks
        );
        entry.putDouble(
                PATH_BLOCKS,
                journey.pathBlocks()
        );
        entry.putString(
                MODE,
                mode.name()
        );
        entry.putInt(
                RUNS,
                previousRuns + 1
        );

        measurements.put(
                key,
                entry
        );
        root.put(
                MEASUREMENTS,
                measurements
        );
        worldData.put(ROOT, root);
        persistence.writeWorldData(worldData);

        return new MeasurementRecord(
                journey.id(),
                mode,
                elapsedTicks,
                journey.pathBlocks(),
                previousRuns + 1
        );
    }

    private Optional<MeasurementRecord> comparisonFor(
            Journey journey,
            MeasurementMode mode
    ) {
        if (!journey.id().equals(
                "g-to-a-learned"
        )) {
            return Optional.empty();
        }

        return readMeasurement(
                "a-to-g-safe",
                MeasurementMode.FIRST
        );
    }

    private Optional<MeasurementRecord> readMeasurement(
            String journeyId,
            MeasurementMode mode
    ) {
        NbtCompound measurements =
                measurementRoot();
        String key =
                measurementKey(journeyId, mode);
        if (!measurements.contains(
                key,
                NbtElement.COMPOUND_TYPE
        )) {
            return Optional.empty();
        }

        NbtCompound entry =
                measurements.getCompound(key);
        return Optional.of(
                new MeasurementRecord(
                        journeyId,
                        mode,
                        entry.getLong(ELAPSED_TICKS),
                        entry.getDouble(PATH_BLOCKS),
                        entry.getInt(RUNS)
                )
        );
    }

    private NbtCompound measurementRoot() {
        NbtCompound worldData =
                WorldRpgPersistentState.get(server)
                        .readWorldData();
        if (!worldData.contains(
                ROOT,
                NbtElement.COMPOUND_TYPE
        )) {
            return new NbtCompound();
        }

        NbtCompound root =
                worldData.getCompound(ROOT);
        if (!root.contains(
                MEASUREMENTS,
                NbtElement.COMPOUND_TYPE
        )) {
            return new NbtCompound();
        }
        return root.getCompound(MEASUREMENTS);
    }

    private GrayboxState requireState() {
        return loadState().orElseThrow(() ->
                new IllegalStateException(
                        "province topology graybox is not built in this world"
                )
        );
    }

    private Optional<GrayboxState> loadState() {
        requireStarted();
        NbtCompound worldData =
                WorldRpgPersistentState.get(server)
                        .readWorldData();

        if (!worldData.contains(
                ROOT,
                NbtElement.COMPOUND_TYPE
        )) {
            return Optional.empty();
        }

        NbtCompound root =
                worldData.getCompound(ROOT);
        if (!root.getBoolean(BUILT)) {
            return Optional.empty();
        }

        return Optional.of(
                new GrayboxState(
                        root.getInt(ORIGIN_X),
                        root.getInt(ORIGIN_Z)
                )
        );
    }

    private Node nearestNode(
            ServerPlayerEntity player,
            GrayboxState state
    ) {
        return java.util.Arrays.stream(Node.values())
                .min(
                        Comparator.comparingDouble(node -> {
                            Point point =
                                    ProvinceGrayboxLayout
                                            .nodePosition(node);
                            return horizontalDistance(
                                    player,
                                    state.absoluteX(point),
                                    state.absoluteZ(point)
                            );
                        })
                )
                .orElse(Node.HOME);
    }

    private static double horizontalDistance(
            ServerPlayerEntity player,
            int x,
            int z
    ) {
        double dx =
                player.getX() - (x + 0.5);
        double dz =
                player.getZ() - (z + 0.5);
        return Math.hypot(dx, dz);
    }

    private static int surfaceY(
            ServerWorld world,
            int x,
            int z
    ) {
        return world.getTopY(
                Heightmap.Type.WORLD_SURFACE,
                x,
                z
        ) - 1;
    }

    private static String measurementKey(
            String journeyId,
            MeasurementMode mode
    ) {
        return journeyId
                + "__"
                + mode.name()
                .toLowerCase(Locale.ROOT);
    }

    private static String format(double value) {
        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "province topology runtime is not started"
            );
        }
    }

    public enum MeasurementMode {
        FIRST,
        KNOWN;

        public static MeasurementMode parse(String text) {
            try {
                return valueOf(
                        text.trim()
                                .toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "measurement mode must be 'first' or 'known'"
                );
            }
        }
    }

    public record GrayboxState(
            int originX,
            int originZ
    ) {
        public int absoluteX(Point point) {
            return originX + point.east();
        }

        public int absoluteZ(Point point) {
            return originZ + point.south();
        }
    }

    private record ActiveMeasurement(
            String journeyId,
            MeasurementMode mode,
            long startedAtTick,
            double startX,
            double startZ
    ) {
    }

    public record MeasurementRecord(
            String journeyId,
            MeasurementMode mode,
            long elapsedTicks,
            double pathBlocks,
            int runCount
    ) {
        public double elapsedMinutes() {
            return elapsedTicks / 1200.0;
        }
    }

    public record StartResult(
            Journey journey,
            MeasurementMode mode,
            int startX,
            int startZ
    ) {
        public String summary() {
            return "topology measurement started | "
                    + journey.id()
                    + " | "
                    + mode.name().toLowerCase(Locale.ROOT)
                    + " | "
                    + journey.start()
                    + " -> "
                    + journey.end()
                    + " | path≈"
                    + Math.round(journey.pathBlocks())
                    + " blocks";
        }
    }

    public record FinishResult(
            Journey journey,
            MeasurementMode mode,
            long elapsedTicks,
            double elapsedMinutes,
            int runCount,
            Optional<MeasurementRecord> comparison
    ) {
        public String summary() {
            String target;
            if (journey.hasTargetBand()) {
                String relation;
                if (elapsedMinutes
                        < journey.targetMinMinutes()) {
                    relation = "under provisional band";
                } else if (elapsedMinutes
                        > journey.targetMaxMinutes()) {
                    relation = "over provisional band";
                } else {
                    relation = "inside provisional band";
                }
                target = " | target="
                        + format(journey.targetMinMinutes())
                        + "-"
                        + format(journey.targetMaxMinutes())
                        + " min"
                        + " | "
                        + relation;
            } else {
                target = " | relative target";
            }

            String comparisonText =
                    comparison.map(previous -> {
                        double previousMinutes =
                                previous.elapsedMinutes();
                        double delta =
                                previousMinutes - elapsedMinutes;
                        double percent =
                                previousMinutes <= 0.0
                                        ? 0.0
                                        : (delta / previousMinutes)
                                        * 100.0;
                        return " | versus first A->G="
                                + format(previousMinutes)
                                + " min"
                                + " | change="
                                + format(percent)
                                + "%";
                    }).orElse("");

            return "topology measurement complete | "
                    + journey.id()
                    + " | "
                    + mode.name().toLowerCase(Locale.ROOT)
                    + " | elapsed="
                    + format(elapsedMinutes)
                    + " min"
                    + " | path≈"
                    + Math.round(journey.pathBlocks())
                    + " blocks"
                    + " | run="
                    + runCount
                    + target
                    + comparisonText;
        }
    }
}
