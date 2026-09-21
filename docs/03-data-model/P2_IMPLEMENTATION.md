# P2 implementation procedure

Status: ACTIVE

Branch: `p2/data-foundation`

Tracking issue: #1

P2 establishes the data and persistence foundation before World RPG creates real content at scale.

## Order of work

### P2.1 — Identity, validation, and immutable registries

Implement a Minecraft-independent foundation where practical:

- `RpgId` stable namespaced identity.
- `RpgDefinition` base definition contract.
- Source-aware validation messages.
- Typed registry keys.
- Immutable definition registries.
- Immutable registry-set snapshots.
- Atomic publication that refuses candidates containing validation errors.
- Unit tests for invalid IDs, duplicate IDs, immutability, type-safe lookup, and failed-publication safety.

This is the first implementation slice.

### P2.2 — Source decoding

Decide and document the runtime authoring representation, then implement:

- source discovery,
- source-location tracking,
- decoding,
- structural validation,
- schema-version handling at the definition layer,
- deterministic ordering.

The authoring format is not frozen merely because JSON exists. The chosen format must work with validation, comments/documentation expectations, tooling, Minecraft/Fabric integration, and future standalone tools.

### P2.3 — References and resolution

Implement explicit reference types:

- required definition reference,
- optional definition reference,
- unresolved source reference,
- resolved typed reference/handle where useful.

Resolution happens against candidate registries before publication.

A missing required reference is an error. Optionality must be explicit in the type/model rather than inferred from null.

### P2.4 — Staged candidate loader

Implement the complete transaction:

source discovery
-> decode
-> structural validation
-> candidate registry construction
-> reference resolution
-> semantic validation
-> cross-registry validation
-> atomic publish.

A failed candidate load leaves the currently active snapshot untouched.

### P2.5 — Developer inspection

Add developer commands/tools to:

- list registry domains,
- list definitions,
- inspect one definition,
- show source origin,
- show active schema/snapshot information,
- report the previous load failure.

Debug output must use IDs and source locations so content errors are traceable.

### P2.6 — Persistence and migrations

Only after definition identity is stable:

- decide player-state and world-state storage ownership,
- evaluate Cardinal Components versus owned storage,
- define save schema versioning,
- implement migration interfaces,
- define missing-definition recovery policies,
- test round trips and migrations.

Persistence must never serialize display names or unstable list indices as identity.

### P2.7 — Reload safety

Every registry domain declares one of:

- SAFE — may be replaced live.
- GUARDED — reload allowed only when runtime constraints are satisfied.
- RESTART — requires restart/save reload.

No global “reload everything” promise.

## P2 exit gate

A tiny authored definition must complete:

source
-> decode
-> validation
-> reference resolution
-> immutable registry snapshot
-> atomic publication
-> developer inspection

and a runtime reference to that definition must persist, reload, and migrate without ambiguity.

Only then does P3 begin.
