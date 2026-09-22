# Planning status

## Current phase

**P4-G — lived-experience reconvergence gate.**

The active implementation branch remains `p4/math-simulator`, but new P4 feature expansion is paused.

This is an intentional project correction, not a rollback.

P1-P4 machinery already implemented remains valuable and is preserved. The project is temporarily prioritizing the missing experience specifications and physical gate evidence that are supposed to govern that machinery.

See:

- `docs/00-constitution/SPIRIT_OF_THE_GAME.md`
- `docs/00-constitution/IDEA_FIDELITY_PACT.md`
- `docs/00-constitution/GROUNDING_GATE.md`
- `docs/00-constitution/P4_G_RECONVERGENCE_GATE.md`

## Grounding state

Conceptual grounding is complete through the first 15-hour lived storyboard:

- player life-cycle;
- session rhythm;
- world/travel rhythm;
- progression doctrine;
- reward doctrine;
- quest doctrine;
- settlement doctrine;
- anti-pattern catalog;
- first 15-hour storyboard.

The first-province topology exists as an authored paper/graybox hypothesis, **not yet as physically walked evidence**.

## Foundation already implemented

- Fabric 1.21.1 / Java 21 project shell and CI.
- Stable RPG IDs and typed registries.
- Staged/validated content publication and persistence foundations.
- Generic P3 stats/resources/conditions/effects/auras/casting/network-authority machinery.
- P3 authored combat-data convergence.
- P4 production damage/healing resolver.
- Deterministic headless simulation sharing production math/cast paths.
- Explicit hit/crit/mitigation/scaling/interrupt/absorb boundaries.
- Provisional routine, attrition, accidental-pull, caster, elite, healer and dungeon stress fixtures.

## P3 physical evidence state

The hardened Minecraft proof harness is implemented and the exact pre-test candidate
`763681e6775058e2cd6c3d8fb9e96333677e7d5f` passed CI run `35770679890`.

A first physical Minecraft run was recorded on 2026-09-22:

- **P3-G2 PASS** — real client/network activation path;
- **P3-G4 PASS** — committed timed Bolt applies its RPG effect only at completion;
- **P3-G10 PARTIAL (strong)** — three distinct scheduled channel ticks observed, but not from a fresh target baseline;
- G1/G3/G6/G12 have useful partial evidence;
- G9 remains unresolved because a later successful Bolt after a 20-block spawn did not record source position at activation;
- G17 remains pending because the supplied probe targeted the player itself rather than the Husk;
- remaining physical rows stay pending.

Evidence:

- `docs/16-implementation/P3_GATE_VERIFICATION.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_RUNBOOK.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_2026-09-22.md`

P3 remains open. The gate is **not** satisfied by the legacy aggregate `P3 proof PASS` helper.

## Important interpretation

The current P4 encounter fixtures are **mechanical stress fixtures and provisional calibration evidence**.

They prove that the plant can express useful combat rhythms.

They do not yet define final classes, enemies, dungeon roles or encounter design.

## Work now authorized

- finish the P3 physical evidence matrix without weakening failed expectations;
- fix real defects discovered by physical evidence at their owning layer;
- walk/measure the cheap first-province graybox topology;
- reconcile evidence into P4-G and phase gates;
- repair tests/docs or implementation defects discovered by this work.

## Work temporarily blocked

Unless directly required by the reconvergence work:

- new combat mechanic families;
- new synthetic encounter archetypes;
- more P4 abstractions;
- mass class/ability/item content;
- mass quest/NPC/profession content;
- final terrain production;
- final UI/VFX production.

## Why

World RPG's primary risk is not failure to build enough systems.

It is building an excellent system factory for a game whose lived experience was never specified deeply enough.

The conceptual grounding package now exists. The remaining reconvergence uncertainty is physical: prove the P3 kernel inside Minecraft and prove that the proposed first-province geography works when walked.

The plant remains subordinate to the game.
