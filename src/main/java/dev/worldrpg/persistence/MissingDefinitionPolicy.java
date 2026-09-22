package dev.worldrpg.persistence;

public enum MissingDefinitionPolicy {
    /**
     * Loading a missing persisted reference is a hard error.
     */
    FAIL,

    /**
     * Keep the stable ID unresolved so later content restoration/migration can
     * recover it without silently deleting player/world state.
     */
    PRESERVE_UNRESOLVED,

    /**
     * Explicitly discard the missing optional reference.
     */
    DROP
}
