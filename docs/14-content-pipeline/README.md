# Content pipeline

Status: PROPOSED

The content pipeline is the production factory.

Expected source domains: abilities, effects/auras, items/sets, mobs, NPCs, loot, quests, dialogue, factions, professions/recipes, zones/POIs, spawn groups, travel and presentation metadata.

Pipeline:

author source -> decode -> schema validation -> semantic validation -> cross-reference resolution -> deterministic generation/derivation -> regression checks -> package/runtime load.

Errors that can corrupt gameplay or references fail the build: duplicate IDs, missing mandatory references, unknown stat/effect IDs, circular mandatory quest dependencies, impossible ranges, nonsensical negative values, invalid loot weights, unknown zones and missing mandatory resources.

Warnings are for valid but suspicious data.

Generated output must be deterministic, stable-order and reviewable.
