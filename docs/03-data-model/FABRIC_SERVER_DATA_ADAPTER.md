# Fabric server-data adapter

Status: IMPLEMENTED

P2 content definitions are discovered from server-data resources under:

```text
data/<namespace>/world_rpg/definitions/**/*.json
```

The physical resource path is provenance and override location only.

Stable definition identity continues to come from the JSON document's explicit `id` field.

## Data-pack override behavior

The adapter uses Minecraft's normal server-data resource manager.

For a given physical resource identifier, the highest-priority active resource pack wins before World RPG sees the source.

Different physical files that declare the same World RPG definition ID remain a duplicate-definition error in the core registry transaction.

## Strict JSON preflight

Before domain decode, the source is checked for duplicate JSON keys at any nesting depth.

Duplicate-key input is rejected rather than silently accepting whichever member a parser happens to keep.

## Atomicity

The adapter produces a `ContentSourceBatch` containing:

- successfully read UTF-8 JSON sources;
- adapter diagnostics.

Adapter diagnostics are merged into the same `ValidationReport` used by the core P2 candidate transaction.

Therefore:

```text
resource read / preflight failure
-> validation error
-> candidate may still be inspected diagnostically
-> publication rejected
-> previous active snapshot preserved
```

The Fabric server-data reload listener runs that transaction during server resource reloads, including `/reload`.

Invalid candidates are logged and rejected without replacing the active last-known-good snapshot.

## Bootstrap contract

`WorldRpgContentRuntime.initialize(catalog)` is one-shot.

The domain catalog is frozen before the Fabric listener is registered.

Later gameplay domains compose into that catalog before initialization rather than mutating a live global registry catalog.

P3 authored aura/ability domains now exercise this path.

## Provenance

Each source records:

```text
pack=<pack id> resource=<namespace:path>
```

Definition identity never derives from that string.

## Developer inspection

Permission-level-2 commands:

```text
/worldrpg content status
/worldrpg content list
/worldrpg content inspect <registry> <id>
```

These inspect only the active published snapshot.

A rejected candidate remains visible through reload diagnostics but does not become active.

## Reload safety

SAFE / GUARDED / RESTART domain policies are enforced against the active snapshot before publication.

## Persistence/migrations

The broader P2 persistence foundation is implemented separately from this adapter:

- stable persisted definition pointers;
- world/player PersistentState ownership spike;
- save-schema migration chain;
- missing-definition recovery policies;
- per-domain authored definition schema migrations.

See the other documents in `docs/03-data-model/` for those contracts.

## Current status

The former pending items in this document—strict duplicate-key rejection, reload-safety enforcement, authored domain registration and persistence/migration foundation—have all been implemented.

Future domains should reuse this transaction rather than creating parallel loaders.
