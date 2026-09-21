# ADR-0002: Server-authoritative gameplay

Status: ACCEPTED

## Context

Single-player still runs a logical server, and multiplayer is a future requirement. Client-owned gameplay truth would make combat, saves and multiplayer unreliable.

## Decision

The logical server decides gameplay outcomes and persistent mutations. Clients send intents and render synchronized results.

## Consequences

Damage, healing, casts, cooldowns, loot, XP, currencies, equipment, quest progress, reputation, professions and world-state transitions are server-owned. Client prediction may later improve responsiveness but never becomes authority.
