package dev.worldrpg.combat.actor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeActorBindingIndexTest {
    @Test
    void sameIntegrationIdentityGetsSameRuntimeActor() {
        RuntimeActorBindingIndex<UUID> index = new RuntimeActorBindingIndex<>();
        UUID identity = UUID.randomUUID();

        CombatActor first = index.bind(identity);
        CombatActor second = index.bind(identity);

        assertSame(first, second);
        assertEquals(1, index.size());
    }

    @Test
    void differentIdentitiesGetDifferentRuntimeActors() {
        RuntimeActorBindingIndex<UUID> index = new RuntimeActorBindingIndex<>();

        CombatActor first = index.bind(UUID.randomUUID());
        CombatActor second = index.bind(UUID.randomUUID());

        assertNotEquals(first.id(), second.id());
        assertEquals(2, index.size());
    }

    @Test
    void unbindAndClearAreExplicitLifecycleBoundaries() {
        RuntimeActorBindingIndex<String> index = new RuntimeActorBindingIndex<>();

        CombatActor before = index.bind("entity-a");
        assertTrue(index.unbind("entity-a"));
        assertFalse(index.find("entity-a").isPresent());

        CombatActor rebound = index.bind("entity-a");
        assertNotEquals(before.id(), rebound.id());

        index.clear();
        assertEquals(0, index.size());

        CombatActor nextServerLifetime = index.bind("entity-b");
        assertEquals(1, nextServerLifetime.id().value());
    }
}
