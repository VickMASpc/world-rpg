package dev.worldrpg.combat.actor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Maps an integration-layer identity key to a runtime-only CombatActor.
 *
 * <p>The key is integration identity, not character-save identity. CombatActorId
 * is deliberately ephemeral and may be reused after the index is cleared.</p>
 */
public final class RuntimeActorBindingIndex<K> {
    private final Map<K, CombatActor> actors = new LinkedHashMap<>();
    private long nextActorId = 1L;

    public CombatActor bind(K key) {
        Objects.requireNonNull(key, "key");

        return actors.computeIfAbsent(
                key,
                ignored -> new CombatActor(new CombatActorId(nextActorId++))
        );
    }

    public Optional<CombatActor> find(K key) {
        return Optional.ofNullable(actors.get(Objects.requireNonNull(key, "key")));
    }

    public boolean unbind(K key) {
        return actors.remove(Objects.requireNonNull(key, "key")) != null;
    }

    public void clear() {
        actors.clear();
        nextActorId = 1L;
    }

    public int size() {
        return actors.size();
    }

    public Map<K, CombatActor> bindings() {
        return Collections.unmodifiableMap(actors);
    }
}
