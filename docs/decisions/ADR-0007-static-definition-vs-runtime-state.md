# ADR-0007: Separate static definitions from runtime state

Status: ACCEPTED

## Context

Mixing authored configuration with mutable play state causes reload, save, migration and synchronization problems.

## Decision

Definitions are immutable loaded game data. Runtime/player/world instances store mutable state and reference definitions by stable ID.

## Consequences

Persistence stores only necessary mutable state plus durable definition references. Reload behavior must explicitly account for active runtime references.
