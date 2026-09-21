# ADR-0006: Single runtime mod artifact initially

Status: ACCEPTED

## Context

The codebase needs strong domain structure, but premature multi-module Gradle complexity does not itself create good architecture.

## Decision

Start as one primary Fabric runtime mod with split common/client source sets and strict package boundaries.

## Consequences

Package dependency discipline is architectural. Tooling may split into JVM-only modules once it needs independence from Minecraft or a clean reusable model layer.
