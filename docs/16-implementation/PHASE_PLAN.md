# Plant implementation phase plan

Status: ACCEPTED — ARCHITECTURAL DEPENDENCY PLAN, NOT TOTAL-PROJECT TIMELINE

> **Scale correction:** this file describes dependency/architecture ordering. It must not be used as a project-completion percentage or as a complete production schedule. In particular, P5/P6/P7 compress most of the game's content, world and presentation production into short labels. The scale-corrected project timeline is `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md`.

P0 Constitution: freeze identity, pacing philosophy, world philosophy and anti-goals.

P1 Platform: Fabric shell, Java, build, CI, source separation, dependency policy, distribution boundaries. Exit: a clean clone builds/runs an empty mod reproducibly.

P2 Data/persistence: stable IDs, typed registries, codecs, staged loading, validation, reference resolution, atomic publish, debug inspection, schema versions, persistence ownership, migrations and reload policy. Exit: a minimal definition survives source -> registry -> inspection -> persisted reference safely.

P3 RPG kernel: stats, modifiers, resources, conditions, effects, auras, event pipeline, targeting, casts, cooldowns and authority. Exit requires both automated proof and the entity-backed Minecraft verification matrix.

P4 Mathematics/simulator: baseline combat/progression formulas and headless simulation sharing production resolver/cast machinery.

**P4-G Lived-experience reconvergence:** restore the missing grounding package, reconcile repository truth, record P3 physical evidence, define first-province/player-life/session/travel/progression/reward/quest/settlement doctrine, and prototype world topology before further P4 feature expansion.

P5 Content pipeline: item generation, loot, quest/dialogue primitives, mob/NPC definitions and validation.

P6 World pipeline: zones/POIs, spawns, patrols, graveyards, travel, discovery and developer authoring tools.

**P5/P6 overlap rule:** content and geography begin co-design earlier than the old strictly linear plan. The first province topology must pressure-test content primitives, and content needs must pressure-test geography. Neither pipeline gets to finalize in isolation.

P7 Client: HUD, action bars, spellbook, character UI, map, dialogue, models/VFX/audio and UI design system.

**P7 scale warning:** this label is an architectural bucket only. UI/UX/icons, models/textures, rigging/animation, VFX, audio/ambience and their asset pipelines are separate continuing production lanes in the master production timeline. They do not occur once and then end before content production.

P8 Production readiness: profiling, migrations, integrity CI, authoring docs and packaging.

P9 Vertical slice: complete roughly level 1-15 province targeting about 12-18 hours for a blind first playthrough, with later-level hooks so the province remains relevant.

**P9 scale warning:** a finished vertical slice is not the end of the project timeline. It proves the production factory and the game experience, after which regional production waves continue toward the complete 1-100 world.

## Sequencing correction

The old phase list was useful architecturally but too linear experientially and too compressed as a measure of production scale.

From P4-G onward, phase progress is not permission to postpone actual world validation.

A cheap authored province/topology prototype must appear before expensive production terrain and before large content volume.

The project should cycle:

idea -> player life -> place/session -> system requirement -> plant -> simulator -> playable evidence -> idea review.

For total-project planning and progress accounting, use `MASTER_PRODUCTION_TIMELINE.md`, which separately tracks the runtime, content, world, models/animation, VFX, UI/icons, audio, items/economy, quests/NPCs/professions and testing lanes.