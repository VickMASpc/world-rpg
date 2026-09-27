# P2 implementation procedure

Status: IMPLEMENTED — HISTORICAL PROCEDURE / CURRENT CONTRACT REFERENCE

Branch: `p2/data-foundation`

Tracking issue: #1

P2 established the data and persistence foundation before World RPG creates real content at scale.

This document preserves the order and architectural intent used to build that foundation. The P2 exit gate has been satisfied on its branch; current project status lives in `docs/STATUS.md`.

## Implemented order of work

### P2.1 — Identity, validation, and immutable registries

Implemented a Minecraft-independent foundation where practical:

- `RpgId` stable namespaced identity.
- `RpgDefinition` base definition contract.
- Source-aware validation messages.
- Typed registry keys.
- Immutable definition registries.
- Immutable registry-set snapshots.
- Atomic publication that refuses candidates containing validation errors.
- Unit tests for invalid IDs, duplicate IDs, immutability, type-safe lookup and failed-publication safety.

### P2.2 — Source decoding

The runtime authoring representation is UTF-8 JSON with:

- server-data source discovery;
- source-location/provenance tracking;
- strict structural preflight including duplicate-key rejection;
- common header decoding;
- schema-version handling;
- deterministic ordering.

The physical file path remains provenance/override location rather than stable gameplay identity.

### P2.3 — References and resolution

Implemented explicit typed required/optional definition references and candidate-snapshot resolution.

A missing required reference is an error. Optionality is explicit rather than inferred from null.

### P2.4 — Staged candidate loader

Implemented transaction:

```text
source discovery
-> strict preflight / decode
-> structural validation
-> candidate registry construction
-> reference resolution
-> semantic validation
-> cross-registry validation
-> reload-safety enforcement
-> atomic publish
```

A failed candidate load leaves the currently active snapshot untouched.

### P2.5 — Developer inspection

Implemented developer inspection for active content status, registry listing and definition inspection with traceable IDs/provenance.

### P2.6 — Persistence and migrations

Implemented the foundation for:

- server-owned world/player PersistentState;
- save schema versions;
- explicit monotonic migration steps;
- stable persisted definition pointers;
- missing-definition recovery policies;
- defensive-copy mutation / dirty-state ownership.

Persistence does not serialize display names or unstable list indices as identity.

### P2.7 — Reload safety

Implemented domain classification and runtime enforcement:

- SAFE — may be replaced live;
- GUARDED — reload only when runtime constraints pass;
- RESTART — cannot be applied live.

No global "reload everything safely" promise exists.

## P2 exit gate

Satisfied on `p2/data-foundation` and exercised by stacked authored P3 combat content:

```text
authored source
-> decode
-> validation
-> reference resolution
-> immutable registry snapshot
-> atomic publication
-> developer inspection
-> stable persisted reference
-> save/load/migration
```

P3/P4 development has already begun on top of this foundation.

The remaining reason P2 issue/PR stay open is stacked-branch integration bookkeeping, not missing P2 mechanism work.
