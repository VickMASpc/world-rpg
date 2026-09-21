# ADR-0001: Freeze Minecraft at 1.21.1

Status: ACCEPTED

## Context

World RPG will accumulate deep integration, a fixed world, long-lived saves and large custom systems. Chasing Minecraft releases would repeatedly consume engineering capacity without necessarily improving the RPG.

## Decision

Minecraft Java 1.21.1 is the frozen production engine target. Moving to another Minecraft version is an engine migration rather than ordinary maintenance.

## Consequences

Content and saves can target a stable platform. Fabric/toolchain patch versions may move under testing while Minecraft remains fixed. Features from later Minecraft releases are not automatically adopted. A future engine migration requires a dedicated ADR, branch, compatibility audit and regression/migration plan.
