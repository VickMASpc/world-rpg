package dev.worldrpg.combat.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePoolTest {
    private static final ResourceKey MANA =
            ResourceKey.of("world_rpg:resource/test_mana");

    @Test
    void strictSpendNeverPartiallyConsumes() {
        ResourcePool mana = new ResourcePool(MANA, 100.0, 30.0);

        ResourceChange failed = mana.spend(40.0);
        assertFalse(failed.fullyApplied());
        assertEquals(30.0, mana.current());

        ResourceChange success = mana.spend(20.0);
        assertTrue(success.fullyApplied());
        assertEquals(10.0, mana.current());
    }

    @Test
    void gainAndDrainClampExplicitly() {
        ResourcePool mana = new ResourcePool(MANA, 100.0, 90.0);

        ResourceChange gain = mana.gain(30.0);
        assertFalse(gain.fullyApplied());
        assertEquals(10.0, gain.applied());
        assertEquals(100.0, mana.current());

        ResourceChange drain = mana.drainUpTo(150.0);
        assertFalse(drain.fullyApplied());
        assertEquals(100.0, drain.applied());
        assertEquals(0.0, mana.current());
    }

    @Test
    void maximumChangePoliciesAreExplicit() {
        ResourcePool ratio = new ResourcePool(MANA, 100.0, 50.0);
        ratio.setMaximum(200.0, ResourceMaximumPolicy.PRESERVE_RATIO);
        assertEquals(100.0, ratio.current());

        ResourcePool missing = new ResourcePool(MANA, 100.0, 50.0);
        missing.setMaximum(250.0, ResourceMaximumPolicy.PRESERVE_MISSING_AMOUNT);
        assertEquals(200.0, missing.current());

        ResourcePool absolute = new ResourcePool(MANA, 100.0, 90.0);
        absolute.setMaximum(70.0, ResourceMaximumPolicy.KEEP_CURRENT);
        assertEquals(70.0, absolute.current());
    }

    @Test
    void resourceSetRejectsDuplicatePool() {
        ResourceSet resources = new ResourceSet();
        resources.add(MANA, 100.0, 100.0);

        boolean threw = false;
        try {
            resources.add(MANA, 50.0, 50.0);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }

        assertTrue(threw);
    }
}
