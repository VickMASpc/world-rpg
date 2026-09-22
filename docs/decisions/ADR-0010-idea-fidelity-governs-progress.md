# ADR-0010: Idea fidelity governs progress

Status: ACCEPTED

## Context

World RPG is intentionally slow, geographically large, progression-heavy, and resistant to many modern convenience patterns.

A long project naturally accumulates local optimizations. Over time, individually reasonable decisions can silently transform the product into a different game.

Technical momentum is therefore not sufficient evidence of progress.

## Decision

Major implementation and content milestones are reviewed against the Idea Fidelity Pact.

When implementation convenience conflicts with an accepted experiential invariant, preserving the invariant is the default.

Changing an invariant requires an explicit documented amendment rather than silent drift.

## Consequences

- PRs document intended experience and drift risks.
- Gate completion includes both technical and idea-fidelity review.
- Tests/simulators should protect experiential invariants where they can be made observable.
- Libraries, Minecraft conventions, and modern genre expectations do not automatically override project doctrine.
- Some technically attractive work may be revised or rejected after fidelity review.
