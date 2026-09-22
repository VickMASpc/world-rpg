package dev.worldrpg.integration.minecraft.p3;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.cast.AbilityActivationResult;
import dev.worldrpg.combat.cast.CastController;
import dev.worldrpg.combat.cast.CastInterruptionReason;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.event.CastCompletedEvent;
import dev.worldrpg.combat.event.CastInterruptedEvent;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.stat.ModifierSource;
import dev.worldrpg.combat.stat.StatModifierOperation;
import dev.worldrpg.integration.minecraft.MinecraftCombatActorBindings;
import dev.worldrpg.integration.minecraft.MinecraftEntityResolver;
import dev.worldrpg.integration.minecraft.MinecraftTargetObservationProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity-backed P3 developer combat runtime.
 *
 * <p>This is an integration proof harness, not final game-state ownership.</p>
 */
public final class P3DeveloperCombatRuntime {
    private static final double MOVEMENT_EPSILON_SQUARED = 1.0e-6;
    private static final long PRUNE_INTERVAL_TICKS = 100L;

    private final MinecraftCombatActorBindings bindings;
    private final Map<UUID, P3EntityCombatState> states =
            new LinkedHashMap<>();

    private MinecraftServer server;
    private long lastPruneTick;

    public P3DeveloperCombatRuntime(
            MinecraftCombatActorBindings bindings
    ) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
    }

    public void start(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        lastPruneTick = server.getTicks();
    }

    public void stop() {
        states.clear();
        server = null;
    }

    public P3AbilityActivationResponse handle(
            ServerPlayerEntity source,
            P3AbilityActivationRequest request
    ) {
        requireStarted();
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(request, "request");

        LivingEntity target = MinecraftEntityResolver.findLiving(
                server,
                request.targetEntityUuid()
        ).orElseThrow(() -> new NoSuchElementException(
                "Target entity is not currently available: "
                        + request.targetEntityUuid()
        ));

        P3EntityCombatState sourceState = state(source);
        P3EntityCombatState targetState = state(target);

        AbilityActivationResult result =
                sourceState.casts().tryActivate(
                        P3FixtureDefinitions.requireAbility(
                                request.abilityId()
                        ),
                        targetState.actor(),
                        server.getTicks()
                );

        if (result.accepted() && result.castId().isPresent()) {
            sourceState.markCastStart(source.getPos());
        }

        return P3AbilityActivationResponse.from(
                request.sequence(),
                result
        );
    }

    public void tick(MinecraftServer tickingServer) {
        if (server == null || server != tickingServer) {
            return;
        }

        long gameTick = server.getTicks();

        for (P3EntityCombatState state :
                List.copyOf(states.values())) {
            if (state.casts().activeCast().isEmpty()) {
                continue;
            }

            Optional<LivingEntity> entity =
                    MinecraftEntityResolver.findLiving(
                            server,
                            state.entityUuid()
                    );

            if (entity.isEmpty() || !entity.get().isAlive()) {
                emitToPlayer(
                        state,
                        state.casts().interrupt(
                                CastInterruptionReason.SOURCE_INVALID,
                                gameTick
                        ).stream().toList()
                );
                state.clearCastStart();
                continue;
            }

            if (state.castStartPosition().isPresent()
                    && entity.get().getPos().squaredDistanceTo(
                            state.castStartPosition().get()
                    ) > MOVEMENT_EPSILON_SQUARED) {
                emitToPlayer(
                        state,
                        state.casts().interrupt(
                                CastInterruptionReason.MOVEMENT,
                                gameTick
                        ).stream().toList()
                );
                state.clearCastStart();
                continue;
            }

            List<CombatEvent> events = state.casts().tick(gameTick);
            emitToPlayer(state, events);

            if (state.casts().activeCast().isEmpty()) {
                state.clearCastStart();
            }
        }

        if (gameTick - lastPruneTick >= PRUNE_INTERVAL_TICKS) {
            pruneMissingEntities();
            lastPruneTick = gameTick;
        }
    }

    public String status(LivingEntity entity) {
        P3EntityCombatState state = state(entity);

        double health = state.actor().resources()
                .require(P3FixtureDefinitions.HEALTH)
                .current();
        double mana = state.actor().resources()
                .require(P3FixtureDefinitions.MANA)
                .current();
        double power = state.actor().stats()
                .value(P3FixtureDefinitions.POWER);

        return "actor=" + state.actor().id().value()
                + " health=" + health
                + " mana=" + mana
                + " power=" + power
                + " auras=" + state.actor().auras().instances().size()
                + " casting=" + state.casts().activeCast().isPresent();
    }

    public boolean removeState(UUID entityUuid) {
        Objects.requireNonNull(entityUuid, "entityUuid");

        P3EntityCombatState removed = states.remove(entityUuid);
        bindings.unbind(entityUuid);
        return removed != null;
    }

    public void reset() {
        states.clear();
        bindings.clear();
    }

    private P3EntityCombatState state(LivingEntity entity) {
        requireStarted();

        return states.computeIfAbsent(
                entity.getUuid(),
                ignored -> createState(entity)
        );
    }

    private P3EntityCombatState createState(LivingEntity entity) {
        CombatActor actor = bindings.bind(entity);

        actor.resources().add(
                P3FixtureDefinitions.HEALTH,
                100.0,
                100.0
        );
        actor.resources().add(
                P3FixtureDefinitions.MANA,
                100.0,
                100.0
        );
        actor.stats().setBase(
                P3FixtureDefinitions.POWER,
                100.0
        );

        if (entity instanceof ServerPlayerEntity) {
            actor.stats().addModifier(
                    P3FixtureDefinitions.POWER,
                    new ModifierSource(
                            P3FixtureDefinitions.STAFF,
                            actor.id().value()
                    ),
                    StatModifierOperation.ADD,
                    10.0,
                    0
            );
        }

        CastController casts = new CastController(
                actor,
                new CooldownBook(),
                new MinecraftTargetObservationProvider(
                        server,
                        bindings
                )
        );

        return new P3EntityCombatState(
                entity.getUuid(),
                actor,
                casts
        );
    }

    private void pruneMissingEntities() {
        List<UUID> missing = states.keySet().stream()
                .filter(uuid ->
                        MinecraftEntityResolver.findLiving(server, uuid)
                                .isEmpty()
                )
                .toList();

        missing.forEach(this::removeState);
    }

    private void emitToPlayer(
            P3EntityCombatState state,
            List<? extends CombatEvent> events
    ) {
        if (events.isEmpty()) {
            return;
        }

        Optional<LivingEntity> entity =
                MinecraftEntityResolver.findLiving(
                        server,
                        state.entityUuid()
                );

        if (entity.isEmpty()
                || !(entity.get() instanceof ServerPlayerEntity player)) {
            return;
        }

        for (CombatEvent event : new ArrayList<>(events)) {
            if (event instanceof CastCompletedEvent completed) {
                player.sendMessage(
                        Text.literal(
                                "P3 cast completed: "
                                        + completed.abilityId()
                        ),
                        false
                );
            } else if (event instanceof CastInterruptedEvent interrupted) {
                player.sendMessage(
                        Text.literal(
                                "P3 cast interrupted: "
                                        + interrupted.reason()
                        ),
                        false
                );
            }
        }
    }

    private void requireStarted() {
        if (server == null) {
            throw new IllegalStateException(
                    "P3 developer combat runtime is not started"
            );
        }
    }
}
