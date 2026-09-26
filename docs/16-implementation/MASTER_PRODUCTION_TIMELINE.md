# World RPG master production timeline

Status: ACCEPTED AS SCALE-CORRECTED MASTER TIMELINE

This document is the project-scale timeline.

Day-to-day development cadence is governed by:

- `docs/16-implementation/DEVELOPMENT_CYCLE_CONTRACT.md`

A development cycle must advance runtime architecture, authored content, player-facing UI/presentation, world integration and physical acceptance together. Individual models, schemas, commands, screens, enemies or quests are subtasks inside those cycles, not milestones by themselves.

`PHASE_PLAN.md` remains useful for architectural dependency order, but it is **not** a measure of total project completion. In particular, the old P5/P6/P7 labels compress most of the game's actual production mass into three short lines.

World RPG is not primarily a framework project. It is a 1-100 slow-burn RPG intended to support roughly 350-500 hours for a blind first character, on a large fixed authored world, with persistent geography, long travel, memorable equipment, long-lived quests, classes, professions, settlements, dungeons, enemies and a full presentation layer.

The project therefore has two fundamentally different kinds of work:

1. **plant work** — runtime, schemas, simulation, authoring tools, validation, export, packaging and reusable pipelines;
2. **game production** — terrain, settlements, dungeons, actors, animations, VFX, audio, UI, items, abilities, quests, dialogue, NPC populations, spawn ecology, encounter design, professions, loot, economy and repeated play/balance passes.

The plant enables production. It does not substitute for production.

---

# 1. Frank scale statement

The repository has advanced much farther in architecture than in game production.

P0-P4/P4-G represent important, expensive foundation work, but relative to the complete 1-100 game they are still only a **small minority of total end-to-end effort**.

It would be misleading to describe the project as being near the middle merely because several numbered architecture phases are implemented.

A more truthful interpretation is:

- core runtime/data/combat foundations: substantially advanced;
- lived-experience doctrine: substantially advanced conceptually;
- physical Minecraft proof: active/incomplete;
- production content factory: early/proposed;
- authored world production: pre-production/graybox;
- final classes/content: largely unproduced;
- items/loot/economy production: largely unproduced;
- mobs/NPC production: largely unproduced;
- quest/dialogue content: largely unproduced;
- professions: largely unproduced;
- UI: draft;
- models/animation: pipeline-level draft, production not begun at scale;
- VFX/presentation: pipeline-level draft, production not begun at scale;
- audio/music/ambience: production not begun at scale;
- complete 1-100 continent: overwhelmingly future work.

A polished first-province vertical slice will be a major milestone because it forces every production line to become real. Even then, the complete game will remain much larger than that slice.

Do not convert architecture-phase count into a completion percentage.

---

# 2. The production dimensions that must be measured

Every future timeline review must explicitly account for the following workstreams.

## A. Runtime / engine / authority

Includes:

- Fabric integration;
- networking and server authority;
- persistent actor state;
- combat/event runtime;
- world-state runtime;
- NPC/mob runtime;
- quest state;
- professions/economy state;
- save migration;
- performance and compatibility.

This is the most advanced workstream today.

## B. Content schemas, compiler and developer tooling

The content factory must support and validate, at minimum:

- abilities/effects/auras;
- items/item sets;
- loot;
- mobs/NPCs;
- factions/reputations;
- quests/dialogue;
- professions/recipes;
- zones/POIs;
- spawn groups/patrols;
- travel nodes/routes;
- presentation metadata;
- asset IDs and export references.

This workstream is not a one-time parser. It requires authoring ergonomics, diagnostics, batch validation, cross-reference checks, generation tools, debug inspection and eventually migration support for large amounts of authored content.

## C. Combat, classes and abilities

A finished class is not a list of damage buttons.

Production includes:

- class identity;
- progression/unlock cadence;
- baseline resource model;
- dozens of ability definitions over the complete journey;
- talents/specialization decisions if retained;
- cast/channel/interrupt interactions;
- status effects;
- utility;
- item synergies;
- enemy interactions;
- class trainers or other acquisition context;
- action-bar/spellbook presentation;
- icons;
- animation hooks;
- VFX;
- SFX;
- tuning across solo, group and expedition rhythms.

The engine primitives are only the substrate.

## D. Items, loot, economy and professions

The complete game is expected to contain **large-scale equipment/content volume**, potentially thousands of equipment/material/consumable/recipe definitions once the full 1-100 journey exists.

Each production item may require some combination of:

- stats;
- requirements;
- unique effects;
- rarity and naming;
- icon;
- held/worn/world model or reusable model assignment;
- tooltip text;
- loot/vendor/quest/crafting source;
- sell/buy/repair values;
- profession relationship;
- region/level relevance;
- set relationship;
- visual identity;
- drop-rate and economy testing.

Professions add their own long-duration progression, trainers, recipes, materials, regional sourcing, rare recipes and economy loops.

This is a major production program, not a late database fill.

## E. World / map / environment production

The fixed authored land is one of the largest project dimensions.

Production includes:

- continent macro-layout;
- region and subregion topology;
- elevation and sightline planning;
- roads, rivers, bridges, passes and barriers;
- travel-time calibration;
- settlement placement;
- graveyards and recovery routes;
- dungeons/caves/ruins;
- landmark placement;
- gathering/resource geography;
- spawn ecology;
- interiors;
- architecture kits;
- terrain detailing;
- vegetation/props;
- lighting/weather presentation;
- navigation readability;
- transport routes;
- repeated walk testing.

A 10-14 km-scale landmass cannot be treated as one late 'map task'. It is a continuing production track that grows region by region.

## F. NPCs, mobs, encounters and dungeons

One enemy archetype can imply:

- model/texture or model variant;
- rig;
- locomotion;
- attacks/casts;
- hit/death/idle animations;
- audio;
- VFX;
- stats;
- ability loadout;
- AI behavior;
- assist/leash behavior;
- spawn groups;
- patrols;
- loot;
- quest references;
- regional ecology;
- difficulty tuning.

Dungeon production adds layouts, encounter scripting/configuration, bosses, trash groups, rewards, checkpoints, environmental art and repeated group testing.

## G. Quests, dialogue, lore and settlement life

The project deliberately rejects a simple 'kill 8 / return' conveyor.

Production therefore includes:

- long-lived quest state machines;
- multi-session chains;
- prerequisite/branch logic;
- NPC relationships;
- geographically grounded objectives;
- discoverable/hidden starts;
- dungeon/class/profession/faction hooks;
- journal text that remains understandable days later;
- dialogue graphs;
- rewards and world-state consequences;
- placement and pacing across expeditions.

This content is writing, world design, implementation and testing simultaneously.

## H. Character, creature and prop models

The asset pipeline must distinguish reusable kits from unique hero assets.

Production includes:

- player equipment silhouettes where needed;
- NPC body/clothing/hair kits;
- enemy creature models;
- bosses;
- pets/summons;
- weapons/tools;
- important world props;
- texture sets;
- LOD/performance rules where needed;
- attachment points;
- collision/scale metadata;
- export/version discipline.

Models are not a single P7 task. They are created continuously as classes, enemies, equipment, settlements, quests and dungeons are authored.

## I. Animation

Animation is its own production line.

It includes:

- locomotion;
- idle sets;
- combat stances;
- melee attacks;
- cast/channel loops;
- ability-specific actions;
- hit/stagger/death;
- interactions;
- NPC ambient behavior;
- creature-specific movement;
- boss mechanics;
- emotes where appropriate;
- animation blending/state integration.

Every new actor family can create new animation debt.

## J. VFX and presentation effects

VFX production includes:

- cast starts;
- projectiles;
- impacts;
- channels/beams;
- persistent auras;
- ground effects;
- healing;
- buffs/debuffs;
- interrupts;
- enemy telegraphs where appropriate;
- boss mechanics;
- environmental effects;
- weather/atmosphere;
- lights;
- camera responses;
- performance/LOD tuning.

Changing Fireball visuals must not change Fireball math, but Fireball is still unfinished as a shipped ability until its visual/audio/presentation path exists.

## K. UI / UX / icons

The project already implies a large application-like interface surface:

- HUD;
- player/target/cast frames;
- action bars;
- character/equipment;
- bags;
- spellbook;
- talents/progression;
- quest log;
- dialogue;
- map/minimap;
- vendor/trainer/repair;
- professions;
- reputation;
- loot;
- death/recovery;
- travel;
- settings;
- character creation;
- developer inspection.

In addition, production needs typography, panels, icon language, rarity/status treatment, tooltips, scaling, focus/input behavior, scrolling, accessibility and consistency.

Icons themselves form a continuing art-production stream for abilities, items, professions, currencies, statuses and map symbols.

## L. Audio and music/ambience

Audio production must account for:

- ability casts/impacts/channels;
- weapon/combat sounds;
- creature vocals;
- NPC/world ambience;
- settlement ambience;
- dungeon ambience;
- biome/region ambience;
- weather;
- UI feedback;
- travel/transport;
- environmental props;
- music or musical ambience if adopted;
- mixing and repetition fatigue.

Audio is part of place memory and combat readability, not optional final polish.

## M. Testing, balancing and performance

Testing includes much more than unit/CI correctness:

- combat balance;
- attrition;
- class progression;
- economy;
- loot rates;
- profession pacing;
- travel time;
- quest continuity;
- navigation readability;
- encounter density;
- dungeon difficulty;
- multiplayer/group behavior where supported;
- save migration;
- client performance;
- server performance;
- asset memory;
- long-session stability;
- regression across a growing content set.

---

# 3. Timeline model: overlapping production eras

These are not isolated boxes. Each era activates more workstreams while earlier ones continue.

## ERA 0 — Idea, constitution and repository plant

**Historical state: substantially complete.**

This era established:

- 'This isn't Minecraft anymore' as the conversion premise;
- slow, spatial, lived-in progression;
- 1-100 as the central game rather than a tutorial for endgame;
- roughly 350-500 hour first-character planning scale;
- fixed authored geography;
- long travel and expedition rhythm;
- persistent resource/attrition intent;
- memorable item pacing;
- long-lived quests;
- settlement continuity;
- Fabric 1.21.1 / Java 21 baseline;
- repository skeleton and architectural boundaries.

It also established the plant vocabulary and the rule that elegant infrastructure is subordinate to lived experience.

## ERA 1 — Data/runtime foundation

**Historical state: substantially complete.**

Includes P1-P2 work:

- build/CI;
- stable IDs;
- registries;
- codecs;
- strict loading;
- validation;
- references;
- persistence/migrations;
- server-data foundation.

This era made large authored content technically possible but did not create that content.

## ERA 2 — RPG kernel, combat mathematics and physical proof

**State: advanced, but not closed. Current project location is here plus P4-G reconvergence.**

Includes:

- stats/resources/modifiers;
- conditions/effects/auras;
- casts/channels/cooldowns;
- authority/networking;
- Minecraft entity binding;
- production combat-resolution boundary;
- hit/crit/mitigation/absorb/scaling work;
- headless simulation;
- encounter stress fixtures;
- P3 physical evidence;
- first-province topology graybox proof.

Exit does **not** mean 'combat is finished'. It means the foundation is trustworthy enough to support production.

## ERA 3 — Production-factory bootstrap

**State: mostly future work. This is the next major project era.**

This era converts today's framework into something capable of producing the game repeatedly.

Parallel tracks:

### Content factory

Build practical authoring and validation for:

- items/sets/loot;
- mobs/NPCs;
- quests/dialogue;
- factions/reputations;
- professions/recipes;
- zones/POIs/spawns/travel;
- presentation metadata.

### Asset factory

Establish:

- model source/export conventions;
- texture conventions;
- rig/animation pipeline;
- attachment standards;
- asset IDs;
- import/export automation where justified;
- asset validation;
- runtime loading/performance rules.

### VFX/presentation factory

Establish data-driven presentation IDs and reusable emitters/attachments/sounds/lights/camera-response patterns.

### UI factory

Prototype technology choice, create design tokens/components, establish HUD/window/tooltips/icons/layout foundations.

### World-authoring factory

Create reproducible world-source conventions, POI IDs, spawn/patrol placement tooling, travel graph inspection and world validation.

### Audio factory

Define source/master/export conventions, IDs, event hooks, mixing categories and reuse rules.

**Era 3 exit:** one complete tiny cross-domain content package can be authored from source and appear correctly in Minecraft with model/animation/VFX/SFX/UI/world placement, then survive validation and reload/build.

This tiny package is deliberately not the vertical slice. It is proof that production lines connect.

## ERA 4 — First-province pre-production

**State: conceptual grounding exists; production specification mostly future.**

The first province becomes a real production plan.

Freeze enough to build without freezing the whole 1-100 game:

- province topology after walking tests;
- terrain/biome palette;
- architecture kits for A/F/G and major ruins;
- visual landmark plan;
- initial class roster for the slice;
- slice ability progression;
- enemy-family roster;
- NPC/service roster;
- item/reward budget;
- profession representation;
- quest-chain map;
- dungeon/deep-ruin plan;
- UI surfaces required by hours 0-15;
- model/animation asset list;
- VFX/SFX list;
- ambience list;
- icon list;
- performance budget;
- testing plan.

Earlier planning used a rough slice target such as two classes, 20-30 quests, 10-15 enemy archetypes, two meaningful settlements plus refuge/support locations and one dungeon. Those remain planning-scale references rather than frozen quotas.

**Era 4 exit:** the province has an explicit production backlog whose asset/content counts can actually be estimated.

## ERA 5 — First-province production wave

**State: future. This is the first truly large content-production era.**

All major workstreams run at once.

### World team/track

- build production terrain;
- roads/rivers/bridges;
- A/F/G settlements;
- interiors;
- dangerous pocket E;
- ruins/caves/dungeon;
- landmarks;
- gathering geography;
- spawn areas;
- travel nodes;
- graveyards;
- lighting/weather pass.

### Actor track

- NPC visual kits;
- enemy families;
- bosses/elites;
- rigs;
- animations;
- attachments;
- sounds;
- VFX hooks.

### Class/combat track

- real slice class progression;
- real abilities;
- icons;
- animations;
- VFX/SFX;
- trainer/acquisition flow;
- enemy interactions;
- balance.

### Item/economy/profession track

- equipment pool;
- memorable uniques;
- consumables/reagents;
- materials;
- loot tables;
- vendor/trainer/repair economy;
- profession recipes and nodes;
- item icons/models;
- source distribution.

### Quest/NPC track

- long chains;
- dialogue;
- journals;
- hidden/conditional starts;
- dungeon hooks;
- class/profession hooks;
- settlement continuity;
- geographic placement.

### UI track

Implement the required subset of the final UI system, not temporary debug screens masquerading as final UX.

### Audio/VFX track

Build enough presentation density that combat and geography are readable and memorable.

**Important:** production does not wait for every asset in one domain before another begins. A settlement, quest chain, mob family or dungeon moves through multiple tracks as a package.

## ERA 6 — First-province integrated alpha

**State: future.**

The province exists end-to-end and can be played for many hours, but quality and completeness are not assumed.

Primary work changes from creation to integration:

- fix missing cross-domain references;
- remove placeholder presentation;
- tune travel time;
- tune combat/attrition;
- tune reward cadence;
- tune economy;
- tune profession pacing;
- validate quest continuity over multiple real sessions;
- validate settlement usefulness;
- fix navigation failures;
- improve encounter ecology;
- profile client/server performance;
- verify save/reload/migration;
- harden tools where real production exposed bottlenecks.

## ERA 7 — Level 1-15 vertical slice release-candidate quality

**State: future.**

Target: roughly 12-18 hours for a blind first playthrough while leaving meaningful unfinished province content and later-level return hooks.

Exit requires lived evidence, not checklist completion.

The player should have:

- a mental map;
- remembered roads/landmarks;
- a remembered item history;
- at least one multi-session quest memory;
- meaningful settlement distinction;
- class familiarity;
- resource/attrition decisions;
- reasons to return later;
- clear evidence that the world continues beyond the slice.

At this point the project has proven the **game**, not merely the engine.

But this is still only the beginning of the full production timeline.

## ERA 8 — Production scaling and regional factory hardening

**State: future.**

The first province will expose expensive manual bottlenecks. Before multiplying content across the continent, improve throughput where evidence justifies it:

- batch authoring/import tools;
- item/loot generation helpers with human review;
- NPC/appearance kit reuse;
- animation reuse/retarget rules;
- architecture/prop kits;
- biome/environment kits;
- VFX libraries;
- icon workflows;
- quest validation/reporting;
- spawn/ecology visualization;
- economy/balance batch simulation;
- world travel graph tooling;
- content regression dashboards;
- asset performance checks.

This era is what prevents the rest of the continent from requiring first-province effort multiplied naïvely by every region.

## ERA 9 — Continental production waves

**State: future and expected to contain the majority of total production effort.**

The 1-100 world should be built in overlapping regional waves, not as one giant final map pass.

A regional wave contains, in parallel:

- topology/world production;
- settlements/architecture/interiors;
- enemy families and variants;
- NPC populations;
- quests/dialogue;
- class progression additions;
- items/loot/economy;
- professions/materials/recipes;
- dungeons/elites/bosses;
- models/textures/animations;
- VFX/audio;
- map/UI data;
- balance/simulation;
- lived-play validation.

Suggested planning bands may follow progression eras rather than equal level chunks. Exact boundaries are not frozen.

The critical rule is that **later region production begins while earlier regions are integrating**, once shared pipelines are stable enough. This creates a production wave, not a serial staircase.

The continent must also preserve return reasons so regional waves are not disposable content islands.

## ERA 10 — Late-game systems, long-tail progression and endgame

**State: future.**

Only after enough of the 1-100 journey is concrete should late-game/endgame systems be finalized.

Potential scope includes:

- highest-level class kits;
- capstone quest lines;
- advanced professions;
- reputations;
- long-tail rare items;
- major dungeons/raids or equivalent group content if retained;
- world bosses/rare ecology;
- transport network maturity;
- high-level return content in older regions;
- economy sinks;
- collection/achievement systems if they serve the game.

Do not design endgame in a vacuum while the leveling game is still hypothetical.

## ERA 11 — Whole-game integration, content lock and balance

**State: future.**

Once the complete journey exists:

- progression pass 1-100;
- economy pass;
- item replacement/history pass;
- class balance pass;
- profession pacing pass;
- quest continuity pass;
- world-return relevance pass;
- travel-network pass;
- dungeon/encounter pass;
- UI consistency pass;
- audio/VFX consistency pass;
- performance optimization;
- asset-memory optimization;
- long-save migration testing;
- multiplayer/server stress as required;
- bug burn-down;
- accessibility/settings hardening;
- packaging/install/update path.

## ERA 12 — Release and post-release operating model

**State: future.**

A world intended to support hundreds of hours also needs maintenance discipline:

- save compatibility;
- content migrations;
- balance changes;
- bug fixes;
- content additions;
- regression CI;
- versioned world/content data;
- patch-note discipline;
- optional expansion pipeline.

---

# 4. Why the timeline is not linear

At maturity, the project should resemble a set of staggered production lanes:

```text
engine/tooling       ========----====---==--------------------------
content schemas      ========--------====----====-------------------
world/map             ---graybox----PROVINCE----REGION----REGION----
classes/combat        =====proof====slice====growth====growth========
items/economy         -----factory---slice====regional==============
quests/dialogue       -----factory---slice====regional==============
mobs/NPCs             -----factory---slice====regional==============
models/animation      -----pipeline--slice====regional==============
VFX                   -----pipeline--slice====regional==============
UI/icons              -----system====slice====expand================
audio                 -----pipeline--slice====regional==============
testing/balance       ===proof===slice===============================
```

The exact line lengths are illustrative.

The important point is that after production begins, there is no single moment called 'do models' or 'do the map'. Those lanes remain active until content lock.

---

# 5. Production-unit accounting

Future status reports must stop using only architecture checkboxes.

Track at least these measurable units for each playable production wave:

## World

- authored area/route coverage;
- settlements/outposts;
- interiors;
- dungeons/ruins/caves;
- landmarks;
- travel nodes/routes;
- spawn regions;
- gathering regions;
- graveyards;
- tested travel-time edges.

## Actors

- unique models;
- reusable model variants;
- texture sets;
- rigs;
- animation sets;
- enemy families;
- bosses/elites;
- NPC appearance kits.

## Presentation

- ability VFX;
- environment VFX;
- actor SFX;
- ability SFX;
- ambience sets;
- UI sounds;
- icons;
- finished UI surfaces.

## Gameplay content

- class progression milestones;
- abilities;
- items/equipment;
- consumables/materials;
- loot tables;
- quests/chains;
- dialogue graphs;
- NPC service definitions;
- professions/recipes;
- factions/reputations;
- encounter packages.

## Quality

For every domain record:

- authored;
- integrated;
- visually presentable;
- mechanically tested;
- lived-play tested;
- performance checked;
- final/polish complete.

An authored JSON entry is not a finished piece of game content.

---

# 6. Current position on the corrected timeline

As of 2026-09-22:

- Era 0: substantially complete;
- Era 1: substantially complete;
- Era 2: advanced, with P3 physical evidence and P4-G physical/topology proof still active;
- Era 3: only fragments exist; the general content/asset/UI/world/audio factories are not yet production-ready;
- Era 4: first-province lived-experience and topology doctrine exist, but the production backlog/art/content specification is not frozen;
- Era 5+: not begun at meaningful production scale.

This is not a failure state.

It is simply a much earlier total-project position than the architectural phase numbers made it appear.

The next correct move is **not** to jump into thousands of items or final terrain.

The next move is to close the current physical proof, then build the cross-domain production factory and turn the grounded first province into an explicit asset/content backlog.

---

# 7. The first major recalibration gate

Before declaring the project 'in vertical-slice production', produce a **First Province Production Ledger** with explicit provisional counts/estimates for:

- world locations and terrain kits;
- settlements/interiors;
- dungeon/ruin spaces;
- NPC roles and appearance variants;
- enemy families and variants;
- bosses/elites;
- class abilities in the slice;
- item/equipment/consumable/material volume;
- loot tables;
- profession recipes/nodes;
- quest chains and dialogue volume;
- models;
- rigs;
- animations;
- VFX;
- audio/ambience;
- icons;
- UI surfaces;
- authoring tools still missing;
- testing passes.

Only then will the timeline have a credible denominator for the first 12-18 hours.

The same accounting method can later be applied region by region toward the complete 1-100 game.

---

# 8. Timeline rule

From this point onward:

**No milestone may claim major project progress solely because a framework or content schema exists.**

Every major progress review must answer two separate questions:

1. **What new capability can the plant express?**
2. **What additional portion of the actual RPG now exists in playable, presentable, authored form?**

If only the first number is moving, the project is building machinery rather than the game.
