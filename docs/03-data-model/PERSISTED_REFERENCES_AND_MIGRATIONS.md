# Persisted references and save migrations

Status: CORE IMPLEMENTED

Runtime saves persist mutable state and durable definition pointers.

They do not serialize immutable authored definitions.

## Durable definition pointer

A persisted pointer contains:

- registry-domain ID,
- definition ID.

Both are stable namespaced RPG IDs.

File paths, display names, array order and Java class names are not persistence
identity.

## Missing-definition policy

Missing persisted references are not silently converted to null.

Each owning domain must eventually choose an explicit policy:

- FAIL — save load cannot continue with the missing definition.
- PRESERVE_UNRESOLVED — retain the durable pointer for later restoration or
  migration.
- DROP — remove an explicitly optional missing reference.

The default policy set is FAIL.

## Save schema

Save schema versions are positive monotonic integers.

Migrations:

- advance exactly one schema version,
- are unique per source version,
- run in deterministic order,
- fail loudly when a required step is missing,
- refuse saves newer than the runtime understands.

## Separation from authored schema

Authored definition schema versions and persisted save schema versions are
different concerns.

Changing the ability JSON schema does not automatically imply a player-save
migration, and changing persisted runtime state does not rewrite authored
definitions.
