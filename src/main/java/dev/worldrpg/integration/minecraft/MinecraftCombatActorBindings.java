package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.actor.RuntimeActorBindingIndex;
import net.minecraft.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Binds currently observed Minecraft living entities to ephemeral RPG combat actors.
 *
 * <p>Minecraft UUID is the bridge key for the current server lifetime. This class
 * does not claim that entity UUID is World RPG character persistence identity.</p>
 */
public final class MinecraftCombatActorBindings {
    private final RuntimeActorBindingIndex<UUID> index =
            new RuntimeActorBindingIndex<>();
    private final Map<CombatActorId, UUID> entityUuidByActor =
            new LinkedHashMap<>();

    public CombatActor bind(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");

        CombatActor actor = index.bind(entity.getUuid());
        entityUuidByActor.putIfAbsent(actor.id(), entity.getUuid());
        return actor;
    }

    public Optional<UUID> entityUuid(CombatActorId actorId) {
        return Optional.ofNullable(
                entityUuidByActor.get(Objects.requireNonNull(actorId, "actorId"))
        );
    }

    public boolean unbind(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return unbind(entity.getUuid());
    }

    public boolean unbind(UUID entityUuid) {
        Objects.requireNonNull(entityUuid, "entityUuid");

        Optional<CombatActor> actor = index.find(entityUuid);
        boolean removed = index.unbind(entityUuid);

        actor.ifPresent(value -> entityUuidByActor.remove(value.id()));
        return removed;
    }

    public int size() {
        return index.size();
    }

    public void clear() {
        index.clear();
        entityUuidByActor.clear();
    }
}
