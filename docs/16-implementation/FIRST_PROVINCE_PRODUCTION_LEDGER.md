# First Province Production Ledger

Status: PROVISIONAL BASELINE — COUNTS ARE PLANNING RANGES, NOT CONTENT QUOTAS

This ledger exists to answer a question the old phase plan could not answer:

**How much actual game must be manufactured for the first 12-18 hours?**

It is the first attempt to give the project a production denominator.

The accepted lived-experience target is not “a town, thirty quests and some enemies.” The opening must create a remembered home, roads, route knowledge, quiet travel, danger seen before it can be conquered, a remote refuge, a regional settlement, long-lived quests, class growth, memorable equipment, professions/economy pressure and unfinished reasons to return.

The ledger therefore counts cross-domain production, not only data definitions.

---

# 1. How to read the ranges

## Confidence

- **H — high:** directly implied by accepted storyboard/topology/doctrine.
- **M — medium:** derived from existing planning references and likely slice needs.
- **L — low:** throughput placeholder used to expose scale; must be replaced during pre-production.

## Important distinction

A content definition is not automatically a unique asset.

Examples:

- 120 equipment definitions do not require 120 unique 3D models.
- 10 enemy families can still require 60+ animation clips.
- 25 quests can reference the same NPCs, landmarks and enemy families.
- 40 NPCs do not require 40 unique body rigs.

Reuse is expected.

However, reuse does not make integration free. Every definition still needs references, placement, validation, tuning and testing.

---

# 2. Scope assumptions for this ledger

These are planning assumptions only.

- one first province;
- roughly 12-18 hours blind first-character play;
- opening progression roughly within the first 10-15 levels, exact level not frozen;
- home settlement A;
- remote refuge F;
- regional settlement G;
- road/fork/river topology from the accepted graybox hypothesis;
- at least one visible early danger pocket E;
- at least one local ruin/cave and one deeper unfinished site;
- at least one dungeon/deep-instance-equivalent production test;
- roughly two representative playable classes for slice throughput planning, without freezing the final roster;
- professions represented enough to prove long-form parallel progression;
- later-level hooks preserved so the province is not disposable.

If pre-production changes these assumptions, the ledger changes rather than forcing the game to obey the ledger.

---

# 3. Executive production estimate

The first province is likely to require on the order of:

| Domain | Provisional range | Confidence |
| --- | ---: | --- |
| major settlement/refuge roles | 3 | H |
| authored route/landmark/world nodes | 12-20 | M |
| major ruin/cave/dungeon spaces | 3-6 | M |
| interiors/service spaces | 12-24 | L |
| enemy families | 8-12 | M |
| enemy variants | 15-25 | L |
| elite/rare/boss packages | 3-6 | M |
| named/service/story NPCs | 25-40 | M |
| ambient NPC population configs | 20-50 | L |
| playable slice classes | 2 reference classes | M |
| unique slice class abilities per class | 8-12 | M |
| ability/rank progression entries per class | 12-20 | L |
| equipment definitions | 80-140 | L |
| memorable unique/set-equivalent equipment | 8-15 | M |
| consumables/reagents | 20-35 | L |
| materials/quest/key items | 40-70 | L |
| loot-table/source definitions | 20-40 | L |
| profession recipes for represented professions | 20-40 | L |
| tracked quest definitions | 20-30 | M |
| substantial multi-step quest chains | 6-10 | M |
| rumors/untracked information hooks | 8-15 | L |
| dialogue/journal prose | ~10k-25k words | L |
| creature/hero base models | 8-14 | L |
| reusable humanoid/NPC appearance kits | 1-2 body bases + 20-40 components | L |
| weapon/tool/important prop model families | 20-40 | L |
| environment/architecture modular pieces | 60-120 | L |
| animation clips after reuse/retargeting | 70-140 | L |
| VFX definitions | 40-80 | L |
| reusable VFX primitives/material patterns | 15-30 | L |
| audio event definitions | 70-140 | L |
| regional/settlement/dungeon ambience sets | 6-12 | L |
| icons/map/status symbols | 120-220 | L |
| required polished UI surfaces for slice | 10-14 | M |
| major integrated playtest hours before slice RC | 150-300 cumulative | L |

These numbers are not promises.

Their purpose is to reveal order of magnitude.

Even the opening province is already a **hundreds-of-assets / hundreds-of-definitions / dozens-of-integrated-systems** production problem.

---

# 4. World and environment ledger

## Required world roles

### A — home settlement

Production package likely includes:

- terrain footprint;
- road exits;
- inn;
- basic vendor/repair;
- early trainer/profession touchpoints;
- several story/service NPCs;
- storage/bank behavior only if final design places it here;
- surrounding working land;
- visual landmark readable from return routes;
- ambient audio;
- local lighting/weather behavior;
- interiors;
- nearby spawn/gathering ecology.

### F — remote refuge

Smaller package, but must visibly change expedition reach.

Likely needs:

- refuge architecture;
- limited services;
- safe stopping/logout behavior;
- several NPCs;
- local ambience;
- approach landmark;
- surrounding danger/spawn relationship.

### G — regional settlement

Broader than A without deleting A.

Likely needs:

- larger architecture kit use;
- more service types;
- trainers/profession/faction differences;
- transport node;
- more NPC density;
- several interiors;
- new outward road connections;
- distinct ambience/visual identity.

## Province world-package range

Planning baseline:

- 3 settlement/refuge roles;
- 12-20 major nodes/landmarks/POIs;
- 4-7 important route corridors;
- 6-10 travel edges that require measured walking tests;
- 2-4 quiet road/wilderness stretches intentionally preserved;
- 1 high-level danger pocket visible early;
- 2-4 ruins/caves/side spaces;
- 1 dungeon or equivalent deep production space;
- 12-24 interiors/service spaces;
- 2-4 architecture/environment kits;
- 60-120 reusable environmental/architecture modules and important props.

## World production states

Each location is tracked as:

1. topology approved;
2. grayboxed;
3. walking/navigation tested;
4. gameplay/spawns integrated;
5. architecture/environment pass;
6. lighting/ambience pass;
7. performance checked;
8. final lived-play approved.

A built structure is not automatically a finished location.

---

# 5. Enemy, NPC and encounter ledger

## Enemy families

Planning range: **8-12 families**.

A family should provide genuine mechanical/geographic identity rather than only stat differences.

Each family package may require:

- one base model/rig or justified reuse;
- one or more texture/visual variants;
- idle;
- locomotion;
- attacks;
- cast/channel animation where relevant;
- hit reaction;
- death;
- audio/vocals;
- VFX hooks;
- stats;
- ability package;
- AI behavior;
- assist/leash rules;
- spawn ecology;
- loot references;
- quest references;
- tuning.

Planning range:

- 8-12 families;
- 15-25 normal variants;
- 2-4 rare/elite variants;
- 1-2 boss-level production packages for the opening slice.

## NPC population

Planning baseline:

- 25-40 named/service/story NPCs;
- 20-50 ambient/population role configs using reusable appearance kits;
- 1-2 humanoid base rigs;
- 20-40 reusable clothing/hair/accessory components before palette/texture variation.

Named NPC count is not the same as unique model count.

## Encounter packages

Track separately:

- routine solo enemies;
- caster/attention enemies;
- multi-pull groups;
- elite/rare encounters;
- dangerous-place E population;
- dungeon trash packages;
- boss encounters;
- patrol/roadside encounters.

Every package requires authored placement plus lived-play testing in actual terrain.

---

# 6. Class and ability ledger

The class roster is not yet frozen.

For the first province, use **two reference classes** only as a throughput assumption until class pre-production deliberately chooses otherwise.

Per reference class, expect approximately:

- 8-12 distinct abilities that matter during the opening slice;
- 12-20 progression/rank/trainer entries including upgrades/utility/passives;
- one resource identity;
- one early talent/progression representation if talents appear in the slice;
- trainer/acquisition flow;
- spellbook/action-bar presentation;
- 8-12 ability icons minimum;
- 6-12 ability VFX/SFX packages;
- shared and class-specific cast/attack animations;
- item/stat interactions;
- solo/attrition/bad-pull/elite validation.

A production ability is not done when its math resolves.

It is done when:

- acquisition exists;
- tooltip/UI exists;
- animation exists;
- VFX exists where appropriate;
- audio exists where appropriate;
- target/cast feedback is readable;
- balance has been tested in actual province encounters.

---

# 7. Items, loot, economy and profession ledger

The opening must already prove item memory without loot spam.

## Equipment

Planning range: **80-140 equipment definitions** across the slice content pool.

This includes equipment the player may never personally see in one playthrough.

Why the number can be much larger than the number of upgrades a player receives:

- multiple supported classes/armor/weapon needs;
- vendor/quest/drop/crafting sources;
- enemy/dungeon tables;
- optional routes;
- rare items;
- deliberate scarcity means many definitions exist in the world without constant replacement.

Within that pool:

- 8-15 should be strong identity/memorable unique or set-equivalent pieces;
- the rest may reuse model families, texture palettes or stat patterns where appropriate.

## Other item content

Planning ranges:

- 20-35 consumables/reagents;
- 25-45 crafting/gathering materials;
- 15-25 quest/key/world-state items;
- 20-40 loot/source-table definitions;
- 8-15 vendor/service inventory definitions;
- 20-40 profession recipes across the profession representation chosen for the slice.

## Economy work

Must separately budget:

- trainer prices;
- repair costs;
- vendor buy/sell values;
- consumable pressure;
- profession costs;
- bag/storage progression;
- transport prices if used;
- savings toward later mount milestone without pretending the mount occurs in the first province.

Economy requires simulation and repeated play. A price table is not a finished economy.

---

# 8. Quest, dialogue and narrative ledger

Existing planning references suggest roughly **20-30 tracked quest definitions** as a plausible first-slice order of magnitude.

The doctrine requires these to be organized around long-lived involvement rather than 20-30 disconnected chores.

Planning structure:

- 6-10 substantial multi-step chains contained within the tracked quest pool;
- local shorter quests used sparingly where they belong;
- 8-15 rumors/information/world hooks that may not become tracked quests immediately;
- at least one chain that survives multiple real sessions;
- at least one class/profession/dungeon-linked thread;
- unfinished hooks into later levels/regions.

Text planning placeholder:

- approximately 10k-25k words across dialogue, journal context, item/world text and quest prose.

This is intentionally low-confidence until chains are outlined.

Every quest package needs:

- state machine;
- conditions;
- objectives;
- NPC/item/world references;
- dialogue;
- journal text;
- geographic directions;
- rewards;
- failure/recovery behavior where applicable;
- save/reload validation;
- actual route/play testing.

---

# 9. Models, textures and rigging ledger

## Actor bases

Planning ranges:

- 1-2 reusable humanoid/NPC base rigs;
- 8-12 creature-family base models/rigs;
- 1-2 boss/hero unique models;
- 2-4 elite/rare visual variants that may build on existing families.

## Appearance components

- 20-40 NPC clothing/hair/accessory components before palette/texture variants;
- texture/palette families sufficient to prevent every NPC in A/F/G from looking cloned.

## Weapons/tools/props

- 20-40 weapon/tool/important portable model families;
- equipment definitions may share those models;
- 60-120 environment/architecture/important-prop modules across settlement and wilderness kits.

## Asset completion states

Track every important asset as:

1. source master exists;
2. texture/material complete;
3. rigged if required;
4. animation-compatible;
5. runtime export validated;
6. scale/attachments/collision verified;
7. integrated in content definition;
8. performance checked.

---

# 10. Animation ledger

Animation volume is one of the largest hidden multipliers.

## Shared humanoid set

Planning baseline: **20-35 clips** covering some combination of:

- idle variants;
- walk/run;
- combat stance;
- melee swings;
- cast start/loop/release;
- channel loop;
- hit;
- death;
- interact/use;
- sit/rest;
- profession interactions;
- selected ambient NPC actions.

## Creature families

Typical family may require **5-10 clips** even after reuse:

- idle;
- locomotion;
- attack(s);
- cast/special;
- hit;
- death;
- optional alert/roar/turn behavior.

Across 8-12 families this alone can create roughly 40-100 family-specific clips.

## Boss/elite and class-specific work

Add:

- 8-15 boss/special-mechanic clips per truly unique boss where required;
- class-specific cast/ability animations where shared humanoid casting is insufficient;
- interaction/quest-specific clips only when they materially improve presentation.

Total planning range after reuse/retargeting: **70-140 animation clips** for a reasonably presentable slice.

This is low-confidence but high-impact and must be measured during Era 3 factory bootstrap.

---

# 11. VFX ledger

Planning baseline:

## Reusable primitives

- 15-30 reusable emitter/material/beam/impact/aura patterns.

## Integrated definitions

- 20-30 class/player ability presentations;
- 15-30 enemy/boss presentations;
- 5-15 world/environment/status presentations.

Total: roughly **40-80 VFX definitions**.

Important distinction:

An effect can reuse primitives while still requiring authored timing, attachments, scale, sound relationships and readability testing.

Every combat VFX must be tested for:

- readability;
- spam density;
- performance;
- relation to cast timing;
- color/status consistency;
- visibility in actual environments.

---

# 12. Audio and ambience ledger

Planning baseline:

- 6-12 ambience beds/sets across home, roads/wild, refuge, regional settlement, dungeon/danger and weather contexts;
- 40-80 combat/ability/actor sound events;
- 15-30 UI/service/world interaction events;
- creature vocal sets for families that need them;
- transport/environment sounds where used.

Total event-definition range: **70-140**.

Audio reuse is expected, but repetition fatigue must be tested because the player may spend hundreds of fights hearing the same family/class.

Music remains a deliberate later decision rather than being assumed simply because the audio pipeline exists.

---

# 13. UI, UX and icon ledger

The full project implies many surfaces. The first province should implement the subset actually required by the opening rather than temporary debug screens.

Planning range: **10-14 polished slice surfaces**, likely drawn from:

- HUD;
- player/target frames;
- cast frame;
- action bar;
- character/equipment;
- bags/inventory;
- spellbook/class progression;
- quest log/journal;
- dialogue;
- map/navigation;
- vendor/trainer/repair;
- profession view if profession gameplay is represented;
- loot/tooltip presentation;
- death/recovery;
- settings/accessibility subset.

Shared design system work includes:

- typography;
- spacing;
- panels/borders;
- buttons/inputs;
- rarity/status color language;
- tooltips;
- scrolling;
- keyboard/mouse behavior;
- scale/resolution handling;
- accessibility rules.

## Icons

Planning range: **120-220 icons/symbols** across:

- abilities;
- items;
- materials;
- consumables;
- professions;
- statuses;
- map/travel markers;
- currencies/services as required.

Icon reuse/category symbols are encouraged where appropriate. A unique icon for every trivial material is not automatically valuable.

---

# 14. Production-factory capabilities required before volume explodes

Before committing to mass first-province content, the project should prove at least these workflows:

## Content/data

- item definition + validation + inspection;
- loot table authoring and probability validation;
- NPC/mob definition + spawn placement;
- quest/dialogue graph authoring and dependency validation;
- profession/recipe authoring;
- zone/POI/travel definitions;
- presentation metadata references;
- batch content report showing missing/broken references.

## World

- stable POI IDs;
- spawn-area placement/inspection;
- patrol/leash visualization or useful diagnostics;
- travel-edge timing/graph inspection;
- reproducible world-source workflow.

## Assets

- Blockbench/model master -> runtime export;
- texture source -> packaged texture;
- rig/animation validation;
- attachment-point convention;
- asset ID/reference validation;
- missing-asset build failure where required.

## VFX/audio

- data-driven presentation ID;
- reusable primitive library;
- ability/event -> VFX/SFX mapping;
- hot/reload-friendly iteration where technically sensible.

## UI

- chosen client UI technology;
- component/design-token library;
- tooltip architecture;
- scalable layout conventions;
- icon registration/loading.

Era 3 should not exit because these systems exist on paper.

It exits when one tiny cross-domain package passes through all of them into Minecraft.

---

# 15. Integrated package test

The production factory should prove itself with a deliberately small **golden content package** before first-province volume begins.

Example package shape:

- one authored enemy;
- one enemy model/texture/rig;
- idle/move/attack/hit/death animation;
- one cast or special action;
- one VFX/SFX presentation;
- one item drop;
- item icon/model assignment;
- one loot table;
- one NPC or quest relationship;
- one world spawn location;
- one journal/UI presentation;
- save/reload persistence;
- validation/build checks.

The exact fiction is not important.

The purpose is to prove the entire factory chain:

source -> validate -> compile/load -> world -> actor -> combat -> presentation -> reward -> UI -> persistence.

Only after this works should production volume increase aggressively.

---

# 16. Testing ledger

A slice cannot reach RC quality through automated tests alone.

Track at least:

## Automated

- schema/content validation;
- runtime unit/integration tests;
- combat simulation;
- loot/economy checks;
- quest dependency checks;
- missing asset/reference checks;
- deterministic generated output;
- CI build/package.

## Physical focused testing

- P3 authority/kernel matrix;
- graybox travel timing;
- route/navigation tests;
- combat readability;
- UI interaction;
- model/animation/VFX integration;
- spawn/leash/patrol behavior;
- dungeon encounter tests.

## Lived play

Before slice RC, planning placeholder: **150-300 cumulative integrated playtest hours** across developers/testers, not one continuous run.

This should include:

- repeated 0-3h opening tests;
- multi-session quest-continuity tests;
- at least several full or near-full 12-18h province runs;
- more than one class path;
- deliberate profession/economy behavior;
- route/navigation memory tests;
- low-guidance blind testers when sufficiently stable.

The 150-300h range is low-confidence and exists only to stop the timeline from pretending a 15-hour game slice can be validated by one 15-hour playthrough.

---

# 17. Completion-state accounting

Every production unit should use the following states where applicable:

1. **designed** — purpose and constraints exist;
2. **authored** — data/source asset exists;
3. **integrated** — appears/functions in runtime;
4. **presentable** — correct non-debug model/UI/VFX/audio exists;
5. **mechanically tested** — behavior works;
6. **lived-play tested** — works in actual session/world context;
7. **performance checked** — acceptable runtime cost;
8. **locked/polished** — approved for slice RC.

Do not count `authored` as `finished`.

A future dashboard should report counts by both domain and state.

Example:

```text
items: 112 authored / 84 integrated / 37 presentable / 22 lived-tested / 0 locked
enemy families: 9 designed / 5 integrated / 3 presentable / 2 lived-tested
quests: 24 authored / 16 integrated / 7 multi-session tested
```

That is more truthful than “items system complete.”

---

# 18. What this ledger tells us about the complete 1-100 game

Do **not** multiply first-province counts by 20 and call that the full-game plan.

The full game benefits from reuse:

- architecture kits;
- humanoid rigs;
- UI system;
- VFX primitives;
- animations;
- item templates;
- content tooling;
- world-authoring tools;
- enemy-family variants;
- profession/economy foundations.

But the full game also introduces new costs:

- new biomes/architecture cultures;
- new enemy families;
- higher-level class abilities;
- new professions/recipes;
- new factions;
- new dungeons/bosses;
- more transport systems;
- thousands of item/content entries;
- more unique presentation;
- balancing across hundreds of hours;
- return-content integration between old and new regions.

Therefore the first province is a **throughput calibration sample**, not a linear estimator.

After Era 7, measure actual production cost per:

- hour of polished content;
- enemy family;
- settlement;
- quest chain;
- dungeon;
- equipment batch;
- class progression band;
- km/route-hour of authored world.

Only then extrapolate the continental schedule.

---

# 19. Immediate implications

The next production-planning steps, after current P3 physical/topology proof, are:

1. bootstrap the Era 3 cross-domain factories;
2. build one golden end-to-end content package;
3. physically validate the province graybox;
4. freeze enough first-province decisions to replace low-confidence ledger ranges with real counts;
5. convert this document into a tracked backlog by domain/state;
6. begin production wave only when the factory can handle revision without manual chaos.

The ledger is not permission to start manufacturing 140 items tomorrow.

It is evidence that **we need the factory before the volume**.

---

# 20. Recalibration rule

Revisit this ledger at least at these points:

- after P3 physical evidence closes;
- after first-province graybox walking tests;
- after the golden cross-domain package works;
- after class/enemy/quest slice rosters are frozen enough for pre-production;
- halfway through first-province alpha;
- at first-province content lock.

At every recalibration, replace guessed ranges with measured counts and measured throughput.

If the real production burden is larger than this ledger, **the timeline grows**.

Do not shrink the game silently to protect an old schedule.
