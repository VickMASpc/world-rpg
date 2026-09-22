package dev.worldrpg.integration.minecraft;

import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.actor.RuntimeActorBindingIndex;
import net.minecraft.entity.LivingEntity;

import java.util.Objects;
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

    public CombatActor bind(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return index.bind(entity.getUuid());
    }

    public boolean unbind(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return index.unbind(entity.getUuid());
    }

    public int size() {
        return index.size();
    }

    public void clear() {
        index.clear();
    }
}
