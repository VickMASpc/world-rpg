package dev.worldrpg.integration.minecraft;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.adventure.ItemContentDefinition;
import dev.worldrpg.content.enemy.EnemyContentDomains;
import dev.worldrpg.content.enemy.LootTableContentDefinition;
import dev.worldrpg.content.enemy.MobContentDefinition;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class AuthoredMobRuntime {
    private final AuthoredMobBindings bindings =
            new AuthoredMobBindings();

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
        bindings.start(server);
    }

    public void stop() {
        bindings.stop();
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

        ServerWorld world = player.getServerWorld();
        BlockPos projected = player.getBlockPos()
                .offset(
                        player.getHorizontalFacing(),
                        distance
                );
        int y = world.getTopY(
                Heightmap.Type.WORLD_SURFACE,
                projected.getX(),
                projected.getZ()
        );
        BlockPos position = new BlockPos(
                projected.getX(),
                y,
                projected.getZ()
        );

        EntityType<?> entityType =
                Registries.ENTITY_TYPE.get(entityTypeId);
        Entity spawned = entityType.spawn(
                world,
                position,
                SpawnReason.COMMAND
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
        // Vanilla attack damage is deliberately neutralized. Authored mobs
        // resolve damage through ProductionCombatRuntime.
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
            mob.setTarget(player);
        }

        // Ordinary player swings cannot mutate this entity's health. The
        // production combat runtime temporarily clears invulnerability only
        // for its canonical defeat bridge.
        living.setInvulnerable(true);

        bindings.bind(
                living.getUuid(),
                definition.id()
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

    public Optional<MobContentDefinition> definition(
            java.util.UUID entityUuid
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
        // Enemy ability/AI scheduling is added by the spawn-ecology wave.
        // This runtime already owns persistence and loot resolution.
    }

    public String statusSummary() {
        requireStarted();
        return "authored mob runtime | bindings="
                + bindings.size();
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

        bindings.unbind(entity.getUuid());

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
        for (var entry : loot.entries()) {
            if (entity.getRandom().nextDouble()
                    > entry.chance()) {
                continue;
            }
            int quantity = entry.minimumQuantity();
            if (entry.maximumQuantity()
                    > entry.minimumQuantity()) {
                quantity += entity.getRandom().nextInt(
                        entry.maximumQuantity()
                                - entry.minimumQuantity()
                                + 1
                );
            }
            items.merge(
                    entry.item().id(),
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

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "authored mob runtime is not started"
            );
        }
    }

    public record SpawnResult(
            MobContentDefinition definition,
            java.util.UUID entityUuid,
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
