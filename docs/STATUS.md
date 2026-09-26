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
- `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`

## Scale interpretation

The numbered P0-P9 architecture phases are **not a project-completion percentage**.

The scale-corrected master timeline separates plant work from the much larger production program: authored world/map, settlements, dungeons, models/textures, animation, VFX, audio, UI/icons, classes, items/economy, professions, mobs/NPCs, quests/dialogue, encounters and repeated integration/balance passes.

Relative to the complete 1-100 RPG, current work is still early in total production even though the runtime/data/combat foundation is substantially advanced.

The current project position is approximately:

- master timeline Era 0 (idea/constitution/repository plant): substantially complete;
- Era 1 (data/runtime foundation): substantially complete;
- Era 2 (RPG kernel/math/physical proof): advanced but open;
- Era 3 (cross-domain production factory): bootstrap begun with the first adventure content graph and persisted quest/reward path, but the broad production factory remains future;
- Era 4 (first-province production specification): conceptual grounding exists and a provisional production ledger now exposes expected production volume, but counts are not frozen;
- Era 5+ (large-scale game production through 1-100): not begun at meaningful production scale.

This interpretation is intentionally frank. Architectural progress is real, but it must not be mistaken for equivalent progress in producing the actual game.

## First-province production scale

The provisional production ledger now gives the first 12-18 hours an explicit planning denominator.

It currently exposes order-of-magnitude ranges such as:

- 3 settlement/refuge roles;
- 12-20 major authored world nodes/landmarks;
- 8-12 enemy families and 15-25 variants;
- 25-40 named/service/story NPCs;
- two reference slice classes for throughput planning, each with roughly 8-12 distinct opening abilities;
- roughly 80-140 equipment definitions plus consumables/materials/quest items;
- roughly 20-30 tracked quest definitions organized around longer chains rather than a conveyor;
- 8-14 creature/hero base models plus reusable humanoid/NPC appearance kits;
- approximately 70-140 animation clips after reuse/retargeting;
- approximately 40-80 VFX definitions;
- approximately 70-140 audio events plus ambience sets;
- approximately 120-220 icons/symbols;
- roughly 10-14 polished UI surfaces required by the opening slice;
- substantial integrated playtest time before release-candidate quality.

These are **planning ranges, not quotas**. They exist to expose hidden production dimensions. They will be recalibrated after graybox travel evidence, the golden cross-domain production-package test, and first-province pre-production.

No project status should claim that vertical-slice production has truly begun merely because schemas or frameworks exist. Era 4 must convert these ranges into a real backlog after physical evidence and production-factory proof.

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


## Production-factory seed now implemented

Direct development has begun the first small cross-domain production path without claiming vertical-slice production.

The active integration branch now contains:

- a combined authored-content catalog spanning combat and adventure definitions;
- first adventure domains for world locations, NPCs, items and quests;
- cross-domain validation so broken NPC/location, quest/NPC, quest/objective or quest/reward references reject the candidate snapshot atomically;
- stable objective keys intended to survive multi-session quest progression and later definition growth;
- per-player persisted quest acceptance, objective progress and completed-quest history;
- a persisted abstract RPG inventory and copper balance;
- typed quest item-stack rewards and an atomic quest turn-in path that grants item/currency rewards while moving the quest into completed history;
- developer commands for quest/inventory inspection and physical proof.

The current first-province definitions are intentionally provisional production-package content. They do **not** freeze final lore, geography, NPC identity, reward balance or quest writing.

The developer commands are proof surfaces, not final player UX. NPC interaction, spatial visit detection, journal UI, real bag/equipment surfaces, world placement, dialogue, models, animation, VFX, audio and final reward presentation remain future connected production work.

This advances Era 3 from "entirely future" to **bootstrap begun**, while Era 2 physical evidence and P4-G remain open. It does not mean the project has entered mass content production.

## First playable-slice candidate

The active integration branch now contains the first player-facing end-to-end adventure loop candidate.

The disposable slice connects the existing production graph to physical Minecraft play:

- `/worldrpg slice build confirm` creates only the physical fixture: a Road Warden post, a short east road and an abandoned checkpoint;
- the Road Warden is a real persistent Minecraft entity representing the authored `world_rpg:npc/first_province/road_warden` definition;
- right-clicking the Warden accepts the authored `east_road_disappearances` quest without using the developer quest-accept command;
- entering the physical checkpoint completes the authored `inspect_route` visit-location objective automatically;
- returning to and interacting with the Warden completes the authored `report_to_warden` speak-to-NPC objective;
- a separate Warden interaction performs canonical quest turn-in and grants the existing persistent RPG item/currency rewards;
- the physical slice anchor is stored in World RPG world persistence, while quest history and RPG inventory remain in player persistence;
- the runtime validates that the authored starter, turn-in NPC and physical visit objective still match the slice contract instead of silently drifting into a parallel hard-coded quest.

The implementation/build candidate is covered by the normal CI build and content-domain tests. **Physical milestone acceptance is still pending the complete manual run** in `docs/16-implementation/FIRST_PLAYABLE_SLICE_TESTING.md`.

Until that physical matrix passes, this should be described as the **first playable-slice candidate**, not proof that the first province or vertical slice is complete.
\n## P3 physical evidence state

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

Likewise, current infrastructure does not imply that the production-heavy dimensions are close to complete. The complete map, actor/model/animation library, VFX/audio library, UI system, large item/economy corpus, professions, authored quests/dialogue, NPC/mob populations and regional content waves remain major future work.

## Work now authorized

- finish the P3 physical evidence matrix without weakening failed expectations;
- fix real defects discovered by physical evidence at their owning layer;
- walk/measure the cheap first-province graybox topology;
- reconcile evidence into P4-G and phase gates;
- plan and bootstrap the cross-domain production factory using the master timeline and production ledger;
- design the golden end-to-end content package that will prove content + world + model/animation + VFX/audio + UI + reward + persistence as one chain;
- refine the First Province Production Ledger into a real backlog after physical and factory evidence;
- repair tests/docs or implementation defects discovered by this work.

## Work temporarily blocked

Unless directly required by the reconvergence or production-factory proof:

- new combat mechanic families;
- new synthetic encounter archetypes;
- more P4 abstractions;
- mass class/ability/item content;
- mass quest/NPC/profession content;
- final terrain production;
- final UI/VFX production.

## Why

World RPG's primary risk is not failure to build enough systems.

It is building an excellent system factory for a game whose lived experience was never specified deeply enough—or mistaking completion of that factory for completion of the game.

The conceptual grounding package now exists. The remaining reconvergence uncertainty is physical: prove the P3 kernel inside Minecraft and prove that the proposed first-province geography works when walked.

After that, the next major challenge is production industrialization: building the connected content, world, asset, UI, VFX and audio pipelines needed to turn the grounded design into thousands of pieces of coherent game content.

The plant remains subordinate to the game.
