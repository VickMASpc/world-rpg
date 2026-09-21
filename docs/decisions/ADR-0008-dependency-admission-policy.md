# ADR-0008: External dependencies require explicit admission

Status: ACCEPTED

## Context

A years-scale mod can become trapped by libraries that own core data formats or stop supporting the frozen engine version.

## Decision

Fabric API is foundational. Other dependencies remain candidates until an evaluation documents the problem solved, coupling, replacement strategy, compatibility and maintenance risk.

## Consequences

Cardinal Components, GeckoLib and UI libraries are not rejected; they are deliberately uncommitted until prototypes prove value. Integration wrappers are preferred when a library touches core domains.
