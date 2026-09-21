# Plant implementation phase plan

Status: ACCEPTED

P0 Constitution: freeze identity, pacing philosophy, world philosophy and anti-goals.

P1 Platform: Fabric shell, Java, build, CI, source separation, dependency policy, distribution boundaries. Exit: a clean clone builds/runs an empty mod reproducibly.

P2 Data/persistence: stable IDs, typed registries, codecs, staged loading, validation, reference resolution, atomic publish, debug inspection, schema versions, persistence ownership, migrations and reload policy. Exit: a minimal definition survives source -> registry -> inspection -> persisted reference safely.

P3 RPG kernel: stats, modifiers, resources, conditions, effects, auras, event pipeline, targeting, casts, cooldowns and authority. Exit: one room, one hostile mob, one class, three abilities, one aura and one item use generic systems.

P4 Mathematics/simulator: baseline combat/progression formulas and headless simulation.

P5 Content pipeline: item generation, loot, quest/dialogue primitives, mob/NPC definitions and validation.

P6 World pipeline: zones/POIs, spawns, patrols, graveyards, travel, discovery and developer authoring tools.

P7 Client: HUD, action bars, spellbook, character UI, map, dialogue, models/VFX/audio and UI design system.

P8 Production readiness: profiling, migrations, integrity CI, authoring docs and packaging.

P9 Vertical slice: complete roughly level 1-15 province targeting about 12-18 hours for a blind first playthrough, with later-level hooks so the province remains relevant.
