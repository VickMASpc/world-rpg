# ADR-0004: Stable namespaced identifiers

Status: ACCEPTED

## Context

Persistent saves and cross-content references cannot safely depend on display names, filenames, list order or coordinates.

## Decision

Definitions receive stable namespaced IDs. Released IDs are durable persistence/API keys.

## Consequences

Renames require aliases or migrations. Human-readable labels may change freely without changing identity. Designed world locations referenced by content also receive semantic IDs.
