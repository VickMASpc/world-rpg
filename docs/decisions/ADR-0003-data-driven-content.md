# ADR-0003: Systems in code, game content in data

Status: ACCEPTED

## Context

World RPG may contain hundreds of abilities, thousands of equipment definitions and hundreds of quests. Bespoke Java implementations do not scale.

## Decision

Reusable mechanics live in code. Game-specific configurations compose those mechanics through validated definitions.

## Consequences

The project invests early in codecs, registries, validation, reference resolution, debug inspection and tooling. Unique content may still require code when it introduces a genuinely new mechanic; composition remains the default.
