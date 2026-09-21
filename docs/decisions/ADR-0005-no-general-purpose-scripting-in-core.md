# ADR-0005: No general-purpose content scripting in core

Status: ACCEPTED

## Context

An embedded Lua/JavaScript-style layer would make arbitrary content easy initially, but would move game logic into a weaker validation/debugging surface and create another runtime/security dependency.

## Decision

Core content uses typed effects, conditions, objectives, actions and event primitives. No general-purpose scripting engine is admitted during the plant phase.

## Revisit trigger

A concrete recurring content requirement cannot be represented cleanly by typed composition without producing worse complexity than a constrained scripting layer.
