package dev.worldrpg.combat.actor;

import dev.worldrpg.combat.aura.AuraContainer;
import dev.worldrpg.combat.resource.ResourceSet;
import dev.worldrpg.combat.stat.StatSheet;

import java.util.Objects;

/**
 * Minecraft-independent runtime combat state.
 *
 * <p>A later integration layer maps Minecraft entities/players to these actors.</p>
 */
public final class CombatActor {
    private final CombatActorId id;
    private final StatSheet stats;
    private final ResourceSet resources;
    private final AuraContainer auras;

    public CombatActor(CombatActorId id) {
        this(id, new StatSheet(), new ResourceSet());
    }

    public CombatActor(
            CombatActorId id,
            StatSheet stats,
            ResourceSet resources
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.auras = new AuraContainer(id, stats);
    }

    public CombatActorId id() {
        return id;
    }

    public StatSheet stats() {
        return stats;
    }

    public ResourceSet resources() {
        return resources;
    }

    public AuraContainer auras() {
        return auras;
    }
}
