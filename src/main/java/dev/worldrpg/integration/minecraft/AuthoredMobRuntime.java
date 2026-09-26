package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.enemy.LootTableContentDefinition;
import dev.worldrpg.content.enemy.MobContentDefinition;
import dev.worldrpg.content.enemy.SpawnGroupContentDefinition;
import dev.worldrpg.content.fabric.WorldRpgContentRuntime;
import dev.worldrpg.player.fabric.MinecraftRpgInventoryRuntime;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AuthoredMobRuntime {
    private static final long AI_TICK_INTERVAL = 5L;
    private static final long POPULATION_TICK_INTERVAL = 20L;

    private final AuthoredMobBindings bindings =
            new AuthoredMobBindings();
    private final Map<RpgId, Long> nextRespawnTick =
            new LinkedHashMap<>();
    private final Map<UUID, UUID> lastTargetByMob =
            new LinkedHashMap<>();

    private MinecraftServer server;
    private boolean eventsRegistered;

    public void registerEvents() {
        if (eventsRegistered) {
            return;
        }
        eventsRegistered = true;

        ServerLivingEntityEvents.AFTER_DEATH.register(
                this::afterDeath
        );
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(
                server,
                "server"
        );
        nextRespawnTick.clear();
        lastTargetByMob.clear();
        bindings.start(server);
    }

    public void stop() {
        bindings.stop();
        nextRespawnTick.clear();
        lastTargetByMob.clear();
        server = null;
    }

    public SpawnResult spawn(
            ServerPlayerEntity player,
            RpgId mobId,
            int distance
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        if (distance < 4 || distance > 40) {
            throw new IllegalArgumentException(
                    "mob spawn distance must be between 4 and 40 blocks"
            );
        }

        MobContentDefinition definition =
                requireMob(mobId);
        ServerWorld world = player.getServerWorld();
        BlockPos projected = player.getBlockPos()
                .offset(
                        player.getHorizontalFacing(),
                        distance
                );
        BlockPos position = surfacePosition(
                world,
                projected.getX(),
                projected.getZ()
        );

        return spawnAt(
                world,
                definition,
                position,
                player,
                null
        );
    }

    public Optional<MobContentDefinition> definition(
            UUID entityUuid
    ) {
        requireStarted();
        return bindings.mobId(
                Objects.requireNonNull(
                        entityUuid,
                        "entityUuid"
                )
        ).map(AuthoredMobRuntime::requireMob);
    }

    public void tick(MinecraftServer tickingServer) {
        if (server == null || server != tickingServer) {
            return;
        }

        long tick = server.getTicks();

        if (tick % POPULATION_TICK_INTERVAL == 0L) {
            maintainSpawnGroups(tick);
        }
        if (tick % AI_TICK_INTERVAL == 0L) {
            tickAuthoredMobs();
        }
    }

    public String statusSummary() {
        requireStarted();
        long grouped = bindings.spawnGroupBindings().size();
        return "authored mob runtime | bindings="
                + bindings.size()
                + " spawnGrouped="
                + grouped;
    }

    private void maintainSpawnGroups(long tick) {
        for (SpawnGroupContentDefinition group :
                spawnGroups().values().stream()
                        .sorted(Comparator.comparing(
                                value -> value.id().toString()
                        ))
                        .toList()) {
            Optional<AdventureWorldBindings.LocationBinding>
                    location =
                    WorldRpgServerRuntime.adventureWorld()
                            .bindings()
                            .locationBinding(
                                    group.location().id()
                            );

            if (location.isEmpty()) {
                continue;
            }

            ServerWorld world = findWorld(
                    location.orElseThrow().dimension()
            );
            if (world == null) {
                continue;
            }

            var binding = location.orElseThrow();
            double centerX = center(
                    binding.minX(),
                    binding.maxX()
            );
            double centerY = center(
                    binding.minY(),
                    binding.maxY()
            );
            double centerZ = center(
                    binding.minZ(),
                    binding.maxZ()
            );

            ServerPlayerEntity nearby =
                    nearestPlayer(
                            world,
                            centerX,
                            centerY,
                            centerZ,
                            group.leashRadius() + 40.0
                    );
            if (nearby == null) {
                continue;
            }

            int alive = 0;
            for (var entry :
                    bindings.spawnGroupBindings().entrySet()) {
                if (!entry.getValue().equals(group.id())) {
                    continue;
                }

                Optional<LivingEntity> entity =
                        MinecraftEntityResolver.findLiving(
                                server,
                                entry.getKey()
                        );
                if (entity.isPresent()
                        && entity.orElseThrow().isAlive()) {
                    alive++;
                    definition(entry.getKey())
                            .ifPresent(definition ->
                                    WorldRpgServerRuntime
                                            .productionCombat()
                                            .registerAuthoredMob(
                                                    entity.orElseThrow(),
                                                    definition
                                            )
                            );
                }
            }

            if (alive >= group.targetPopulation()) {
                continue;
            }

            long next = nextRespawnTick.getOrDefault(
                    group.id(),
                    0L
            );
            if (tick < next) {
                continue;
            }

            int deficit = group.targetPopulation() - alive;
            for (int i = 0; i < deficit; i++) {
                BlockPos spawnPos = randomSpawnPosition(
                        world,
                        centerX,
                        centerZ,
                        group.spawnRadius()
                );
                spawnAt(
                        world,
                        requireMob(group.mob().id()),
                        spawnPos,
                        nearby,
                        group.id()
                );
            }

            nextRespawnTick.put(
                    group.id(),
                    tick + group.respawnTicks()
            );
        }
    }

    private void tickAuthoredMobs() {
        for (var entry :
                bindings.mobBindings().entrySet()) {
            UUID entityUuid = entry.getKey();
            Optional<LivingEntity> entity =
                    MinecraftEntityResolver.findLiving(
                            server,
                            entityUuid
                    );
            if (entity.isEmpty()
                    || !(entity.orElseThrow()
                    instanceof MobEntity mob)
                    || !mob.isAlive()) {
                continue;
            }

            MobContentDefinition definition =
                    requireMob(entry.getValue());

            WorldRpgServerRuntime.productionCombat()
                    .registerAuthoredMob(
                            mob,
                            definition
                    );

            Optional<RpgId> groupId =
                    bindings.spawnGroupId(entityUuid);
            if (groupId.isPresent()
                    && enforceLeash(
                    mob,
                    groupId.orElseThrow()
            )) {
                continue;
            }

            ServerPlayerEntity target = nearestPlayer(
                    mob.getServerWorld(),
                    mob.getX(),
                    mob.getY(),
                    mob.getZ(),
                    definition.aggroRange()
            );

            if (target == null) {
                mob.setTarget(null);
                lastTargetByMob.remove(entityUuid);
                continue;
            }

            UUID previous =
                    lastTargetByMob.put(
                            entityUuid,
                            target.getUuid()
                    );
            mob.setTarget(target);

            if (!target.getUuid().equals(previous)) {
                WorldRpgServerRuntime.productionCombat()
                        .activateMob(
                                mob,
                                definition.engageAbility().id(),
                                mob
                        );
                assistPack(
                        mob,
                        target,
                        definition.assistRange(),
                        groupId.orElse(null)
                );
            }

            if (mob.squaredDistanceTo(target)
                    <= definition.attackRange()
                    * definition.attackRange()
                    && mob.canSee(target)) {
                var response =
                        WorldRpgServerRuntime
                                .productionCombat()
                                .activateMob(
                                        mob,
                                        definition.primaryAbility().id(),
                                        target
                                );
                if (response.accepted()) {
                    mob.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    private boolean enforceLeash(
            MobEntity mob,
            RpgId groupId
    ) {
        Optional<SpawnGroupContentDefinition> group =
                spawnGroups().find(groupId);
        if (group.isEmpty()) {
            return false;
        }

        Optional<AdventureWorldBindings.LocationBinding>
                location =
                WorldRpgServerRuntime.adventureWorld()
                        .bindings()
                        .locationBinding(
                                group.orElseThrow()
                                        .location()
                                        .id()
                        );
        if (location.isEmpty()) {
            return false;
        }

        var binding = location.orElseThrow();
        double centerX = center(
                binding.minX(),
                binding.maxX()
        );
        double centerY = center(
                binding.minY(),
                binding.maxY()
        );
        double centerZ = center(
                binding.minZ(),
                binding.maxZ()
        );

        double dx = mob.getX() - centerX;
        double dz = mob.getZ() - centerZ;
        double distanceSquared = dx * dx + dz * dz;
        double leash = group.orElseThrow().leashRadius();

        if (distanceSquared <= leash * leash) {
            return false;
        }

        mob.setTarget(null);
        lastTargetByMob.remove(mob.getUuid());
        mob.getNavigation().startMovingTo(
                centerX,
                centerY,
                centerZ,
                1.1
        );
        return true;
    }

    private void assistPack(
            MobEntity source,
            ServerPlayerEntity target,
            double assistRange,
            RpgId spawnGroupId
    ) {
        if (assistRange <= 0.0) {
            return;
        }

        for (var entry :
                bindings.mobBindings().entrySet()) {
            if (entry.getKey().equals(source.getUuid())) {
                continue;
            }
            if (spawnGroupId != null
                    && !bindings.spawnGroupId(entry.getKey())
                    .filter(spawnGroupId::equals)
                    .isPresent()) {
                continue;
            }

            Optional<LivingEntity> candidate =
                    MinecraftEntityResolver.findLiving(
                            server,
                            entry.getKey()
                    );
            if (candidate.isEmpty()
                    || !(candidate.orElseThrow()
                    instanceof MobEntity ally)
                    || !ally.isAlive()) {
                continue;
            }

            if (ally.squaredDistanceTo(source)
                    <= assistRange * assistRange) {
                ally.setTarget(target);
                lastTargetByMob.put(
                        ally.getUuid(),
                        target.getUuid()
                );
            }
        }
    }

    private SpawnResult spawnAt(
            ServerWorld world,
            MobContentDefinition definition,
            BlockPos position,
            ServerPlayerEntity initialTarget,
            RpgId spawnGroupId
    ) {
        Identifier entityTypeId = Identifier.tryParse(
                definition.minecraftEntityType().toString()
        );
        if (entityTypeId == null
                || !Registries.ENTITY_TYPE.containsId(
                entityTypeId
        )) {
            throw new IllegalStateException(
                    "unknown Minecraft entity type: "
                            + definition.minecraftEntityType()
            );
        }

        EntityType<?> entityType =
                Registries.ENTITY_TYPE.get(entityTypeId);
        Entity spawned = entityType.spawn(
                world,
                position,
                SpawnReason.EVENT
        );

        if (!(spawned instanceof LivingEntity living)) {
            if (spawned != null) {
                spawned.discard();
            }
            throw new IllegalStateException(
                    definition.minecraftEntityType()
                            + " did not spawn a living entity"
            );
        }

        living.setCustomName(
                Text.literal(
                        definition.displayName()
                                + " [Lv. "
                                + definition.level()
                                + "]"
                )
        );
        living.setCustomNameVisible(true);

        applyAttribute(
                living,
                EntityAttributes.GENERIC_MAX_HEALTH,
                definition.maximumHealth()
        );
        applyAttribute(
                living,
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.0
        );
        applyAttribute(
                living,
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                definition.movementSpeed()
        );
        living.setHealth(
                (float) definition.maximumHealth()
        );

        if (living instanceof MobEntity mob) {
            mob.setPersistent();
            if (initialTarget != null
                    && initialTarget.squaredDistanceTo(mob)
                    <= definition.aggroRange()
                    * definition.aggroRange()) {
                mob.setTarget(initialTarget);
            }
        }

        living.setInvulnerable(true);

        bindings.bind(
                living.getUuid(),
                definition.id(),
                spawnGroupId
        );
        WorldRpgServerRuntime.productionCombat()
                .registerAuthoredMob(
                        living,
                        definition
                );

        return new SpawnResult(
                definition,
                living.getUuid(),
                position
        );
    }

    private void afterDeath(
            LivingEntity entity,
            DamageSource damageSource
    ) {
        if (server == null) {
            return;
        }

        Optional<RpgId> mobId =
                bindings.mobId(entity.getUuid());
        if (mobId.isEmpty()) {
            return;
        }

        Optional<RpgId> spawnGroupId =
                bindings.spawnGroupId(entity.getUuid());
        bindings.unbind(entity.getUuid());
        lastTargetByMob.remove(entity.getUuid());

        spawnGroupId.ifPresent(groupId -> {
            long delay = spawnGroups()
                    .find(groupId)
                    .map(
                            SpawnGroupContentDefinition::respawnTicks
                    )
                    .orElse(200L);
            nextRespawnTick.put(
                    groupId,
                    server.getTicks() + delay
            );
        });

        Entity attacker = damageSource.getAttacker();
        if (!(attacker instanceof ServerPlayerEntity player)) {
            return;
        }

        MobContentDefinition mob = requireMob(
                mobId.orElseThrow()
        );
        LootTableContentDefinition loot =
                WorldRpgContentRuntime.publisher()
                        .active()
                        .require(
                                EnemyContentDomains.LOOT_TABLES
                        )
                        .require(mob.lootTable().id());

        Map<RpgId, Integer> items =
                new LinkedHashMap<>();
        for (var lootEntry : loot.entries()) {
            if (entity.getRandom().nextDouble()
                    > lootEntry.chance()) {
                continue;
            }
            int quantity =
                    lootEntry.minimumQuantity();
            if (lootEntry.maximumQuantity()
                    > lootEntry.minimumQuantity()) {
                quantity += entity.getRandom().nextInt(
                        lootEntry.maximumQuantity()
                                - lootEntry.minimumQuantity()
                                + 1
                );
            }
            items.merge(
                    lootEntry.item().id(),
                    quantity,
                    Math::addExact
            );
        }

        long copper = rollCopper(
                entity,
                loot.minimumCopper(),
                loot.maximumCopper()
        );

        MinecraftRpgInventoryRuntime.grant(
                player,
                items,
                copper
        );

        WorldRpgServerRuntime.adventureWorld()
                .onMobDefeated(
                        player,
                        mob.id()
                );

        player.sendMessage(
                Text.literal(
                        mob.displayName()
                                + " defeated. RPG loot: "
                                + lootSummary(items, copper)
                ),
                false
        );
    }

    private static void applyAttribute(
            LivingEntity entity,
            net.minecraft.registry.entry.RegistryEntry<
                    net.minecraft.entity.attribute.EntityAttribute
                    > attribute,
            double value
    ) {
        EntityAttributeInstance instance =
                entity.getAttributeInstance(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private static BlockPos surfacePosition(
            ServerWorld world,
            int x,
            int z
    ) {
        return new BlockPos(
                x,
                world.getTopY(
                        Heightmap.Type.WORLD_SURFACE,
                        x,
                        z
                ),
                z
        );
    }

    private static BlockPos randomSpawnPosition(
            ServerWorld world,
            double centerX,
            double centerZ,
            double radius
    ) {
        int integerRadius =
                Math.max(1, (int) Math.floor(radius));
        int x = (int) Math.floor(centerX)
                + world.getRandom().nextInt(
                integerRadius * 2 + 1
        )
                - integerRadius;
        int z = (int) Math.floor(centerZ)
                + world.getRandom().nextInt(
                integerRadius * 2 + 1
        )
                - integerRadius;
        return surfacePosition(world, x, z);
    }

    private ServerWorld findWorld(
            String dimension
    ) {
        for (ServerWorld world : server.getWorlds()) {
            if (world.getRegistryKey()
                    .getValue()
                    .toString()
                    .equals(dimension)) {
                return world;
            }
        }
        return null;
    }

    private ServerPlayerEntity nearestPlayer(
            ServerWorld world,
            double x,
            double y,
            double z,
            double range
    ) {
        ServerPlayerEntity best = null;
        double bestDistance = range * range;

        for (ServerPlayerEntity player :
                server.getPlayerManager()
                        .getPlayerList()) {
            if (player.getServerWorld() != world
                    || player.isSpectator()
                    || player.isCreative()
                    || !player.isAlive()) {
                continue;
            }

            double distance =
                    player.squaredDistanceTo(
                            x,
                            y,
                            z
                    );
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private static double center(
            int minimum,
            int maximum
    ) {
        return (minimum + maximum) / 2.0;
    }

    private static long rollCopper(
            LivingEntity entity,
            long minimum,
            long maximum
    ) {
        if (minimum == maximum) {
            return minimum;
        }

        double fraction = entity.getRandom()
                .nextDouble();
        return minimum + (long) Math.floor(
                fraction * (maximum - minimum + 1.0)
        );
    }

    private static String lootSummary(
            Map<RpgId, Integer> items,
            long copper
    ) {
        String itemText = items.entrySet()
                .stream()
                .map(entry -> {
                    String name =
                            WorldRpgContentRuntime.publisher()
                                    .active()
                                    .require(
                                            AdventureContentDomains.ITEMS
                                    )
                                    .find(entry.getKey())
                                    .map(
                                            ItemContentDefinition::displayName
                                    )
                                    .orElse(
                                            entry.getKey().toString()
                                    );
                    return name + " x" + entry.getValue();
                })
                .reduce((left, right) ->
                        left + ", " + right
                )
                .orElse("no items");

        return itemText + ", " + copper + " copper";
    }

    private static MobContentDefinition requireMob(
            RpgId mobId
    ) {
        return WorldRpgContentRuntime.publisher()
                .active()
                .require(EnemyContentDomains.MOBS)
                .find(mobId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "mob definition is not loaded: "
                                        + mobId
                        )
                );
    }

    private static dev.worldrpg.api.registry.DefinitionRegistry<
            SpawnGroupContentDefinition>
    spawnGroups() {
        return WorldRpgContentRuntime.publisher()
                .active()
                .require(
                        EnemyContentDomains.SPAWN_GROUPS
                );
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "authored mob runtime is not started"
            );
        }
    }

    public record SpawnResult(
            MobContentDefinition definition,
            UUID entityUuid,
            BlockPos position
    ) {
        public String summary() {
            return "spawned authored mob "
                    + definition.id()
                    + " | "
                    + definition.displayName()
                    + " lv=" + definition.level()
                    + " hp=" + definition.maximumHealth()
                    + " at="
                    + position.getX() + ","
                    + position.getY() + ","
                    + position.getZ()
                    + " | renderShell="
                    + definition.minecraftEntityType()
                    + " sourceAsset="
                    + definition.sourceAsset();
        }
    }
}
