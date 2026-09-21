# World RPG documentation

This directory is the project's design and architecture source of truth. Documentation is versioned beside implementation because the project is too large to rely on unwritten conventions.

## Reading order

1. 00-constitution — product identity and design boundaries.
2. 01-platform — engine, Fabric baseline, packaging and dependencies.
3. 02-runtime-architecture — subsystem and server/client boundaries.
4. 03-data-model — IDs, definitions, runtime state and migrations.
5. 04 through 15 — domain bibles.
6. 16-implementation — implementation order and repository skeleton.
7. decisions — durable Architecture Decision Records.

## Status vocabulary

- DRAFT: exploratory.
- PROPOSED: specified enough for review.
- ACCEPTED: implementation may depend on it.
- FROZEN: changes require explicit migration/ADR review.
- SUPERSEDED: historical, replaced elsewhere.

## Production gate

A system is not ready for mass content until its document defines ownership, data model, stable references, runtime lifecycle, validation, persistence/network implications, debugging and testing.
