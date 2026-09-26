package dev.worldrpg.integration.minecraft.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.event.CombatActorDefeatedEvent;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.math.ProfiledCombatResolver;
import dev.worldrpg.combat.math.ProfiledCombatStatResolver;
import dev.worldrpg.combat.math.SeededCombatRollSource;
import dev.worldrpg.combat.math.WorldRpgDefenseDraft;
import dev.worldrpg.combat.math.WorldRpgOutcomeDraft;
import dev.worldrpg.combat.math.WorldRpgRatingDraft;
import dev.worldrpg.combat.math.CombatMathStats;
import dev.worldrpg.combat.resolution.CombatMagnitudeRequest;
import dev.worldrpg.combat.resolution.CombatResolutionGateway;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.content.combat.P3CombatContentRuntime;
import dev.worldrpg.content.enemy.MobContentDefinition;
import dev.worldrpg.integration.minecraft.MinecraftCombatActorBindings;
import dev.worldrpg.integration.minecraft.MinecraftEntityResolver;
import dev.worldrpg.integration.minecraft.MinecraftTargetObservationProvider;
import dev.worldrpg.integration.minecraft.WorldRpgServerRuntime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ProductionCombatRuntime
        implements CombatResolutionGateway {
    private static final StatKey LEVEL =
            StatKey.of("world_rpg:stat/level");

    private final MinecraftCombatActorBindings bindings =
            new MinecraftCombatActorBindings();
    private final Map<UUID, ProductionCombatState> states =
            new LinkedHashMap<>();
    private final Map<CombatActorId, Integer> levels =
            new LinkedHashMap<>();

    private MinecraftServer server;
    private ProfiledCombatResolver resolver;

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");

        var levelSource =
                (dev.worldrpg.combat.math.CombatLevelSource) this::levelOf;

        resolver = new ProfiledCombatResolver(
                WorldRpgCombatProfile.MATH,
                new SeededCombatRollSource(
                        server.getOverworld().getSeed()
                                ^ 0x57525047434F4D42L
                ),
                new ProfiledCombatStatResolver(
                        WorldRpgRatingDraft.referenceProfile(),
                        levelSource
                ),
                levelSource,
                WorldRpgOutcomeDraft.referenceProfiles()
        );
    }

    public void stop() {
        states.clear();
        levels.clear();
        bindings.clear();
        resolver = null;
        server = null;
    }

    public void tick(MinecraftServer tickingServer) {
        if (server == null || server != tickingServer) {
            return;
        }

        long tick = server.getTicks();

        for (ProductionCombatState state :
                List.copyOf(states.values())) {
            Optional<LivingEntity> entity =
                    MinecraftEntityResolver.findLiving(
                            server,
                            state.entityUuid()
                    );

            if (entity.isEmpty() || !entity.get().isAlive()) {
                removeState(state.entityUuid());
                continue;
            }

            processEvents(
                    state.casts().tick(tick)
            );

            if (entity.get() instanceof ServerPlayerEntity
                    && tick % 10L == 0L) {
                var focus = state.actor().resources()
                        .require(WorldRpgCombatProfile.FOCUS);
                if (focus.current() < focus.maximum()) {
                    focus.gain(2.0);
                }
            }
        }
    }

    public ProductionCombatState registerAuthoredMob(
            LivingEntity entity,
            MobContentDefinition definition
    ) {
        requireStarted();
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(definition, "definition");

        return states.computeIfAbsent(
                entity.getUuid(),
                ignored -> createMobState(
                        entity,
                        definition
                )
        );
    }

    public ProductionAbilityActivationResponse activatePlayer(
            ServerPlayerEntity player,
            RpgId abilityId,
            UUID targetEntityUuid
    ) {
        requireStarted();
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(abilityId, "abilityId");

        LivingEntity target =
                MinecraftEntityResolver.findLiving(
                        server,
                        Objects.requireNonNull(
                                targetEntityUuid,
                                "targetEntityUuid"
                        )
                ).orElseThrow(() ->
                        new NoSuchElementException(
                                "target entity is unavailable"
                        )
                );

        ProductionCombatState sourceState =
                playerState(player);
        ProductionCombatState targetState =
                stateFor(target);

        var result = sourceState.casts().tryActivate(
                P3CombatContentRuntime.active()
                        .requireAbility(abilityId),
                targetState.actor(),
                server.getTicks()
        );

        processEvents(result.events());

        return new ProductionAbilityActivationResponse(
                result.accepted(),
                result.castId().isPresent(),
                result.validation().failures()
        );
    }

    public ProductionAbilityActivationResponse activateMob(
            MobEntity mob,
            RpgId abilityId,
            LivingEntity target
    ) {
        requireStarted();
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(target, "target");

        ProductionCombatState sourceState =
                stateFor(mob);
        ProductionCombatState targetState =
                stateFor(target);

        var result = sourceState.casts().tryActivate(
                P3CombatContentRuntime.active()
                        .requireAbility(abilityId),
                targetState.actor(),
                server.getTicks()
        );

        processEvents(result.events());

        return new ProductionAbilityActivationResponse(
                result.accepted(),
                result.castId().isPresent(),
                result.validation().failures()
        );
    }

    public Optional<CombatSnapshot> snapshotIfCombatant(
            LivingEntity entity
    ) {
        Objects.requireNonNull(entity, "entity");

        if (entity instanceof ServerPlayerEntity) {
            return Optional.of(snapshot(entity));
        }

        if (WorldRpgServerRuntime.authoredMobs()
                .definition(entity.getUuid())
                .isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(snapshot(entity));
    }

    public CombatSnapshot snapshot(LivingEntity entity) {
        ProductionCombatState state = stateFor(entity);
        var health = state.actor().resources()
                .require(WorldRpgCombatProfile.HEALTH);
        var focus = state.actor().resources()
                .require(WorldRpgCombatProfile.FOCUS);

        return new CombatSnapshot(
                entity.getUuid(),
                state.level(),
                health.current(),
                health.maximum(),
                focus.current(),
                focus.maximum(),
                state.casts().activeCast()
                        .map(cast -> cast.ability().id())
                        .orElse(null),
                state.casts().activeCast()
                        .map(cast -> cast.endsAtTick())
                        .orElse(-1L)
        );
    }

    @Override
    public dev.worldrpg.combat.condition.ConditionResult validate(
            CombatMagnitudeRequest request
    ) {
        requireStarted();
        return resolver.validate(request);
    }

    @Override
    public List<CombatEvent> resolve(
            CombatMagnitudeRequest request
    ) {
        requireStarted();
        return resolver.resolve(request);
    }

    private ProductionCombatState stateFor(
            LivingEntity entity
    ) {
        ProductionCombatState existing =
                states.get(entity.getUuid());
        if (existing != null) {
            return existing;
        }

        if (entity instanceof ServerPlayerEntity player) {
            return playerState(player);
        }

        MobContentDefinition definition =
                WorldRpgServerRuntime.authoredMobs()
                        .definition(entity.getUuid())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "entity is not an authored RPG combatant: "
                                                + entity.getUuid()
                                )
                        );

        return registerAuthoredMob(
                entity,
                definition
        );
    }

    private ProductionCombatState playerState(
            ServerPlayerEntity player
    ) {
        return states.computeIfAbsent(
                player.getUuid(),
                ignored -> createPlayerState(player)
        );
    }

    private ProductionCombatState createPlayerState(
            ServerPlayerEntity player
    ) {
        CombatActor actor = bindings.bind(player);
        actor.resources().add(
                WorldRpgCombatProfile.HEALTH,
                WorldRpgCombatProfile.PLAYER_MAX_HEALTH,
                WorldRpgCombatProfile.PLAYER_MAX_HEALTH
        );
        actor.resources().add(
                WorldRpgCombatProfile.FOCUS,
                WorldRpgCombatProfile.PLAYER_MAX_FOCUS,
                WorldRpgCombatProfile.PLAYER_MAX_FOCUS
        );
        actor.stats().setBase(
                LEVEL,
                1.0
        );
        actor.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                WorldRpgCombatProfile.PLAYER_ATTACK_POWER
        );
        actor.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(1)
        );

        return createState(
                player,
                actor,
                1,
                WorldRpgCombatProfile.PLAYER_MAX_HEALTH
        );
    }

    private ProductionCombatState createMobState(
            LivingEntity entity,
            MobContentDefinition definition
    ) {
        CombatActor actor = bindings.bind(entity);
        actor.resources().add(
                WorldRpgCombatProfile.HEALTH,
                definition.maximumHealth(),
                definition.maximumHealth()
        );
        actor.resources().add(
                WorldRpgCombatProfile.FOCUS,
                100.0,
                100.0
        );
        actor.stats().setBase(
                LEVEL,
                definition.level()
        );
        actor.stats().setBase(
                CombatMathStats.ATTACK_POWER,
                definition.attackDamage()
        );
        actor.stats().setBase(
                CombatMathStats.ARMOR,
                WorldRpgDefenseDraft.referenceArmor(
                        definition.level()
                ) * 0.35
        );

        return createState(
                entity,
                actor,
                definition.level(),
                definition.maximumHealth()
        );
    }

    private ProductionCombatState createState(
            LivingEntity entity,
            CombatActor actor,
            int level,
            double maximumHealth
    ) {
        levels.put(actor.id(), level);

        CastController casts = new CastController(
                actor,
                new CooldownBook(),
                new MinecraftTargetObservationProvider(
                        server,
                        bindings
                )
        );

        ProductionCombatState state =
                new ProductionCombatState(
                        entity.getUuid(),
                        actor,
                        casts,
                        level,
                        maximumHealth
                );

        return state;
    }

    private int levelOf(CombatActor actor) {
        Integer level = levels.get(actor.id());
        if (level == null) {
            throw new IllegalStateException(
                    "missing combat level for actor "
                            + actor.id().value()
            );
        }
        return level;
    }

    private void processEvents(
            List<? extends CombatEvent> sourceEvents
    ) {
        if (sourceEvents.isEmpty()) {
            return;
        }

        List<CombatEvent> events =
                new ArrayList<>(sourceEvents);

        for (CombatEvent event : events) {
            if (event instanceof ResourceChangedEvent changed
                    && changed.resource().equals(
                    WorldRpgCombatProfile.HEALTH
            )) {
                syncVanillaHealth(
                        changed.actorId()
                );
            }

            if (event instanceof CombatActorDefeatedEvent defeated) {
                bridgeDefeat(defeated);
            }
        }
    }

    private void syncVanillaHealth(
            CombatActorId actorId
    ) {
        bindings.entityUuid(actorId)
                .flatMap(uuid ->
                        MinecraftEntityResolver.findLiving(
                                server,
                                uuid
                        )
                )
                .ifPresent(entity -> {
                    ProductionCombatState state =
                            states.get(entity.getUuid());
                    if (state == null) {
                        return;
                    }

                    double current = state.actor()
                            .resources()
                            .require(
                                    WorldRpgCombatProfile.HEALTH
                            )
                            .current();

                    if (current <= 0.0) {
                        return;
                    }

                    float vanillaMaximum =
                            entity.getMaxHealth();
                    float mapped = (float) Math.max(
                            1.0,
                            vanillaMaximum
                                    * current
                                    / state.maximumHealth()
                    );
                    entity.setHealth(
                            Math.min(vanillaMaximum, mapped)
                    );
                });
    }

    private void bridgeDefeat(
            CombatActorDefeatedEvent defeated
    ) {
        Optional<UUID> targetUuid =
                bindings.entityUuid(
                        defeated.targetActorId()
                );
        Optional<UUID> sourceUuid =
                bindings.entityUuid(
                        defeated.sourceActorId()
                );

        if (targetUuid.isEmpty()) {
            return;
        }

        Optional<LivingEntity> target =
                MinecraftEntityResolver.findLiving(
                        server,
                        targetUuid.orElseThrow()
                );
        if (target.isEmpty()) {
            return;
        }

        LivingEntity victim = target.orElseThrow();
        victim.setInvulnerable(false);

        Entity source = sourceUuid
                .flatMap(uuid ->
                        MinecraftEntityResolver.findLiving(
                                server,
                                uuid
                        ).map(value -> (Entity) value)
                )
                .orElse(null);

        if (source instanceof ServerPlayerEntity player) {
            victim.damage(
                    player.getDamageSources()
                            .playerAttack(player),
                    Float.MAX_VALUE
            );
        } else if (source != null) {
            victim.damage(
                    source.getDamageSources()
                            .mobAttack(
                                    (net.minecraft.entity.LivingEntity) source
                            ),
                    Float.MAX_VALUE
            );
        } else {
            victim.kill();
        }
    }

    private void removeState(UUID entityUuid) {
        ProductionCombatState removed =
                states.remove(entityUuid);
        if (removed != null) {
            levels.remove(removed.actor().id());
        }
        bindings.unbind(entityUuid);
    }

    private void requireStarted() {
        if (server == null || resolver == null) {
            throw new IllegalStateException(
                    "production combat runtime is not started"
            );
        }
    }

    public record CombatSnapshot(
            UUID entityUuid,
            int level,
            double health,
            double maximumHealth,
            double focus,
            double maximumFocus,
            RpgId activeAbilityId,
            long castEndsAtTick
    ) {
    }
}
