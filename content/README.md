# RPG content source

This tree is reserved for human-authored game definitions.

Expected domains will be introduced only as their schemas become accepted: abilities, effects/auras, items/sets, mobs, NPCs, loot, quests, dialogue, factions, professions/recipes, zones/POIs, spawn groups and travel.

Rules:

- definitions use stable namespaced IDs,
- cross-references are validated,
- runtime state never belongs here,
- generated bulk output goes under generated/,
- examples do not silently become schema contracts.

The first real content files arrive during P2/P3 as minimal fixtures for registry and combat-kernel tests.
