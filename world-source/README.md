# Fixed-world source

Status: RESERVED WORLD-AUTHORING ROOT — GRAYBOX PROOF ACTIVE, PRODUCTION STORAGE DECISION OPEN

The authored RPG world is a first-class project artifact.

This root will own source metadata/tooling inputs for:

- continent topology;
- regions/subregions;
- roads/travel graph;
- semantic POIs;
- spawn areas;
- graveyards;
- transport nodes;
- discovery regions;
- world-state anchors;
- world package/version metadata.

See:

- `docs/09-world-travel/FIRST_PROVINCE_TOPOLOGY_PROOF.md`
- `docs/14-content-pipeline/ERA3_PRODUCTION_FACTORY_BOOTSTRAP.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`

## Current boundary

The first-province topology is still a disposable graybox hypothesis. Do not treat its provisional geometry or node timings as production terrain truth.

Era 3 only needs enough world-source plumbing to prove:

- one stable POI ID;
- one spawn/actor placement;
- one runtime mapping to physical coordinates/world state;
- one inspectable/debuggable world metadata path;
- revision without hidden manual state.

## Production-world storage gate

Large binary world data may require a storage strategy outside ordinary Git history.

Before first-province **production terrain** begins, explicitly decide:

- which world artifacts are authoritative source;
- which are generated/exported;
- whether Git LFS or another retained binary store is required;
- backup/versioning policy;
- CI/package access;
- world package/version metadata;
- migration/update behavior.

This decision is not required to build the tiny golden dev POI or disposable travel graybox.

It is required before large authored terrain becomes expensive enough that moving it later would be dangerous.
