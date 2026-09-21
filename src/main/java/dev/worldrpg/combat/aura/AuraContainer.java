package dev.worldrpg.combat.aura;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.stat.ModifierSource;
import dev.worldrpg.combat.stat.StatSheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Runtime aura state owned by one target actor.
 */
public final class AuraContainer {
    private final CombatActorId owner;
    private final StatSheet stats;
    private final Map<AuraInstanceId, AuraInstance> instances = new LinkedHashMap<>();
    private long nextInstanceId = 1L;

    public AuraContainer(CombatActorId owner, StatSheet stats) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    public AuraApplicationResult apply(
            AuraDefinition definition,
            CombatActorId source,
            long gameTick
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(source, "source");
        requireTick(gameTick);

        Optional<AuraInstance> existing = findMatching(definition, source);

        if (existing.isPresent()) {
            AuraInstance instance = existing.get();
            int previousStacks = instance.stacks();
            int currentStacks = Math.min(
                    definition.maxStacks(),
                    previousStacks + 1
            );

            instance.setStacks(currentStacks);
            instance.setExpiresAtTick(refreshExpiry(instance, definition, gameTick));
            refreshStatModifiers(instance);

            return new AuraApplicationResult(
                    instance,
                    false,
                    previousStacks,
                    currentStacks
            );
        }

        AuraInstanceId id = new AuraInstanceId(nextInstanceId++);
        AuraInstance instance = new AuraInstance(
                id,
                definition,
                source,
                owner,
                gameTick,
                1,
                initialExpiry(definition, gameTick)
        );

        instances.put(id, instance);
        refreshStatModifiers(instance);

        return new AuraApplicationResult(instance, true, 0, 1);
    }

    public Optional<AuraRemoval> remove(
            AuraInstanceId id,
            AuraRemovalReason reason
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(reason, "reason");

        AuraInstance removed = instances.remove(id);
        if (removed == null) {
            return Optional.empty();
        }

        stats.removeModifiersFrom(modifierSource(removed));
        return Optional.of(new AuraRemoval(removed, reason));
    }

    public List<AuraRemoval> expireDue(long gameTick) {
        requireTick(gameTick);

        List<AuraInstanceId> due = instances.values().stream()
                .filter(instance -> instance.expiresAtTick().isPresent())
                .filter(instance -> instance.expiresAtTick().getAsLong() <= gameTick)
                .map(AuraInstance::id)
                .toList();

        List<AuraRemoval> removals = new ArrayList<>();
        for (AuraInstanceId id : due) {
            remove(id, AuraRemovalReason.EXPIRED).ifPresent(removals::add);
        }

        return List.copyOf(removals);
    }

    public boolean has(RpgId auraDefinitionId) {
        Objects.requireNonNull(auraDefinitionId, "auraDefinitionId");
        return instances.values().stream()
                .anyMatch(instance -> instance.definition().id().equals(auraDefinitionId));
    }

    public Optional<AuraInstance> find(AuraInstanceId id) {
        return Optional.ofNullable(instances.get(Objects.requireNonNull(id, "id")));
    }

    public List<AuraInstance> instances() {
        return List.copyOf(instances.values());
    }

    public Map<AuraInstanceId, AuraInstance> asMap() {
        return Collections.unmodifiableMap(instances);
    }

    private Optional<AuraInstance> findMatching(
            AuraDefinition definition,
            CombatActorId source
    ) {
        return instances.values().stream()
                .filter(instance -> instance.definition().id().equals(definition.id()))
                .filter(instance -> switch (definition.uniqueness()) {
                    case PER_TARGET -> true;
                    case PER_SOURCE -> instance.source().equals(source);
                    case INDEPENDENT -> false;
                })
                .findFirst();
    }

    private void refreshStatModifiers(AuraInstance instance) {
        ModifierSource source = modifierSource(instance);
        stats.removeModifiersFrom(source);

        for (AuraStatModifier modifier : instance.definition().statModifiers()) {
            stats.addModifier(
                    modifier.stat(),
                    source,
                    modifier.operation(),
                    modifier.amountPerStack() * instance.stacks(),
                    modifier.priority()
            );
        }
    }

    private static ModifierSource modifierSource(AuraInstance instance) {
        return new ModifierSource(
                instance.definition().id(),
                instance.id().value()
        );
    }

    private static OptionalLong initialExpiry(
            AuraDefinition definition,
            long gameTick
    ) {
        if (definition.durationTicks().isEmpty()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(safeAdd(
                gameTick,
                definition.durationTicks().getAsLong()
        ));
    }

    private static OptionalLong refreshExpiry(
            AuraInstance instance,
            AuraDefinition definition,
            long gameTick
    ) {
        if (definition.durationTicks().isEmpty()) {
            return OptionalLong.empty();
        }

        long duration = definition.durationTicks().getAsLong();

        return switch (definition.refreshPolicy()) {
            case RESET_DURATION -> OptionalLong.of(safeAdd(gameTick, duration));
            case EXTEND_DURATION -> OptionalLong.of(
                    instance.expiresAtTick().isPresent()
                            ? safeAdd(instance.expiresAtTick().getAsLong(), duration)
                            : safeAdd(gameTick, duration)
            );
            case KEEP_EXISTING -> instance.expiresAtTick();
        };
    }

    private static long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static void requireTick(long gameTick) {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }
}
