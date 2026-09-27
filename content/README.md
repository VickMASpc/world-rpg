# RPG content source

Status: RESERVED SOURCE ROOT — ERA 3 FACTORY BOOTSTRAP PLANNED

This tree owns **human-authored game definitions**.

It is not a dump for generated runtime output and it is not permission to mass-produce content before the authoring/validation factory is proven.

See:

- `docs/14-content-pipeline/README.md`
- `docs/14-content-pipeline/ERA3_PRODUCTION_FACTORY_BOOTSTRAP.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`

Expected eventual domains include:

- abilities;
- effects/auras;
- items/sets;
- mobs;
- NPCs;
- loot;
- quests;
- dialogue;
- factions/reputations;
- professions/recipes;
- zones/POIs;
- spawn groups;
- travel;
- presentation metadata.

Domains are introduced only when their first real schema/use case is ready. Do not create empty directory theater for the whole future game.

Rules:

- definitions use stable namespaced IDs;
- cross-references are validated;
- runtime state never belongs here;
- generated/compiled bulk output goes under `generated/` or the appropriate packaged runtime location;
- generated output is never the only editable source truth;
- examples and developer fixtures do not silently become production schema contracts;
- one Java class per content entry is rejected;
- broken mandatory references fail loudly rather than being ignored.

## Next production proof

Era 3 will build a deliberately tiny developer-only **golden content package** that connects item + loot + mob/NPC + quest + world placement + presentation + UI + persistence.

Only after that package survives validation, runtime integration, save/reload and deliberate revision should first-province content volume increase aggressively.
