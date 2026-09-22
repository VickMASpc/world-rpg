# Fabric persistent runtime state

Status: PERSISTENCE SPIKE IMPLEMENTED

World RPG uses one server-owned persistent state stored through the overworld
PersistentStateManager.

The overworld is storage ownership only. Persisted RPG state is not limited to
gameplay physically occurring in the overworld.

## Root save shape

Current schema: 1

```text
schema: integer
world: compound
players:
  <player UUID>: compound
```

P2 intentionally treats `world` and each player compound as opaque owned
payloads.

Later phases define their fields.

This prevents P2 from prematurely inventing schemas for levels, quests,
equipment, currencies, reputation or world flags.

## Mutation contract

The state returns defensive NBT copies.

Callers must write modified payloads back through:

- writeWorldData
- writePlayerData
- removePlayerData

Those mutations mark the PersistentState dirty.

## Durable definition references

A helper codec persists definition pointers as:

```text
registry: stable namespaced registry ID
definition: stable namespaced definition ID
```

The actual definition object is never embedded in the save.

## Developer persistence probe

Permission-level-2 commands:

```text
/worldrpg persistence status
/worldrpg persistence setref <registry> <definition>
/worldrpg persistence showref
/worldrpg persistence clearref
```

`setref` writes a durable definition pointer into the invoking player's
persistent payload.

`showref` reports the stored pointer and whether the active content snapshot
currently resolves it.

An unresolved reference remains stored until an owning migration/policy chooses
otherwise.

## Migration

The root schema is passed through the generic SaveMigrator before current-state
decode.

Current schema 1 has no historical migration steps yet.

When schema 2 is introduced, a real 1 -> 2 migration must be registered before
schema 2 can become current.
