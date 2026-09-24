package dev.worldrpg.api.data;

import dev.worldrpg.api.id.RpgId;

/**
 * Base contract for immutable authored World RPG definitions.
 */
public interface RpgDefinition {
    RpgId id();
}
