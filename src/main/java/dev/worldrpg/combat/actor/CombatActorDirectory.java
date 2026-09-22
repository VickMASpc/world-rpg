package dev.worldrpg.combat.actor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Minecraft-independent lookup of currently active combat actors.
 *
 * <p>The Fabric integration layer will own registration/removal as Minecraft
 * entities enter and leave RPG combat scope.</p>
 */
public final class CombatActorDirectory {
    private final Map<CombatActorId, CombatActor> actors = new LinkedHashMap<>();

    public void register(CombatActor actor) {
        Objects.requireNonNull(actor, "actor");

        CombatActor previous = actors.putIfAbsent(actor.id(), actor);
        if (previous != null) {
            throw new IllegalArgumentException(
                    "Duplicate combat actor id: " + actor.id()
            );
        }
    }

    public Optional<CombatActor> find(CombatActorId id) {
        return Optional.ofNullable(actors.get(Objects.requireNonNull(id, "id")));
    }

    public CombatActor require(CombatActorId id) {
        return find(id).orElseThrow(() ->
                new NoSuchElementException("Missing combat actor: " + id));
    }

    public boolean remove(CombatActorId id) {
        return actors.remove(Objects.requireNonNull(id, "id")) != null;
    }

    public Map<CombatActorId, CombatActor> actors() {
        return Collections.unmodifiableMap(actors);
    }
}
