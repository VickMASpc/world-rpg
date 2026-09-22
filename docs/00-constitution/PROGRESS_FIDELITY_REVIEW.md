# Progress fidelity review

Status: ACCEPTED

Every major gate, milestone, or architecture-changing PR receives an idea-fidelity review.

This is not a ceremony for its own sake. It exists because World RPG's primary failure mode is gradual normalization into a more convenient, faster, flatter RPG.

## Review lenses

### Memory

Does this help players remember places, items, routes, enemies, people, or decisions?

### Distance

Does this preserve physical scale where scale is meaningful?

### Return

Does this create reasons to leave civilization and reasons to come back?

### Rhythm

Does this preserve contrast between quiet, routine, dangerous, and climactic play?

### Commitment

Do important actions still require planning, time, resources, or position when appropriate?

### Accumulation

Does the character/world retain history rather than constantly replacing it?

### Restraint

Are we resisting unnecessary convenience, noise, inflation, and system bloat?

### Respect

Does the design consume time meaningfully rather than deleting or wasting it?

## Required milestone note

For any gate that changes player-facing behavior, record:

- intended experience,
- invariant(s) touched,
- behavior added,
- behavior deliberately not added,
- risks of drift,
- proof/test evidence,
- unresolved questions.

## Red flags

Stop and review if a change introduces any of these without an explicit design decision:

- instant travel replacing a physical route,
- automatic full recovery after encounters,
- global enemy scaling,
- rapid item replacement,
- disposable zone structure,
- GPS-style navigation becoming required,
- constant combat density,
- mandatory high APM for routine enemies,
- uncontrolled proc/VFX spam,
- quest design optimized around throughput rather than adventure,
- town functions becoming accessible everywhere,
- persistence shortcuts that make long-lived character state fragile,
- hard-coded content that bypasses the plant,
- library constraints dictating game rules,
- convenience features whose main justification is “players expect this now.”

## Green flags

Healthy progress often looks like:

- a system becomes more explicit without changing its intended feel,
- a data model allows content variation rather than forcing one pattern,
- a tool helps us author more faithfully,
- a test protects a doctrine-derived invariant,
- a simulator exposes padding or inflation,
- a mechanic gains a deliberate failure/recovery path,
- content remains relevant longer,
- world knowledge becomes more useful with time.

## Review outcome

A milestone can be:

- **Aligned** — implementation supports the idea.
- **Aligned with watchpoints** — acceptable, but named drift risks remain.
- **Needs correction** — technically useful work should be revised before expansion.
- **Idea amendment required** — implementation cannot proceed without explicitly changing doctrine.

Silence is not approval.
