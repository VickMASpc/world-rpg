# Fabric server-data adapter

Status: IMPLEMENTED CORE

P2 content definitions are discovered from server-data resources under:

```text
data/<namespace>/world_rpg/definitions/**/*.json
```

The physical resource path is provenance and override location only.

Stable definition identity continues to come from the JSON document's explicit
`id` field.

## Data-pack override behavior

The adapter uses Minecraft's normal server-data resource manager.

For a given physical resource identifier, the highest-priority active resource
pack wins before World RPG sees the source.

Different physical files that declare the same World RPG definition ID remain
a duplicate-definition error in the core registry transaction.

This gives us both:

- normal datapack path override semantics,
- stable explicit RPG IDs independent of file location.

## Atomicity

The adapter produces a `ContentSourceBatch`:

- successfully read UTF-8 JSON sources,
- adapter diagnostics.

Adapter diagnostics are merged into the same `ValidationReport` used by the
core P2 candidate transaction.

Therefore:

```text
resource read failure
-> validation error
-> candidate may still be inspected
-> publication rejected
-> previous active snapshot preserved
```

A broken physical resource cannot silently disappear and cause a partial
registry set to publish.

## Provenance

Each source records:

```text
pack=<pack id> resource=<namespace:path>
```

Definition identity never derives from that string.

## Still pending

- actual Fabric reload-listener registration,
- developer inspection commands,
- strict duplicate JSON-key rejection,
- runtime reload-safety enforcement,
- authored P3 definition domains.
