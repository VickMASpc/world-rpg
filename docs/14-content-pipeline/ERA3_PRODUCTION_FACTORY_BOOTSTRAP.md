# Era 3 — production factory bootstrap

Status: PLANNED — IMPLEMENTATION BLOCKED UNTIL CURRENT PHYSICAL RECONVERGENCE EVIDENCE IS SUFFICIENT

This document defines the next major engineering era after the current P3/P4-G physical proof.

The objective is not to make lots of content.

The objective is to make **one piece of cross-domain content cheap, inspectable and safe to author**, then prove that the same path can scale.

The accepted repository boundaries remain:

- `content/` — human-authored RPG definitions;
- `assets-source/` — editable model/texture/UI/icon/VFX/audio masters;
- `world-source/` — fixed-world semantic source/metadata;
- `tools/` — validators, compiler/generator, exporters, inspectors and simulations;
- `generated/` — disposable deterministic generated output;
- `src/main/resources/` — packaged runtime resources where appropriate;
- `src/main/java/` / `src/client/java/` — runtime code, not content volume.

Do not create a second parallel content architecture.

---

# 1. Era 3 exit condition

Era 3 is complete only when a deliberately tiny **golden content package** can travel through this entire chain:

```text
human-authored definition
+ editable source asset
+ semantic world placement
        ↓
strict decode
        ↓
schema validation
        ↓
semantic/cross-reference validation
        ↓
asset/world reference validation
        ↓
deterministic compile/export
        ↓
runtime load
        ↓
Minecraft world/entity
        ↓
model + animation + VFX + SFX
        ↓
combat/interaction
        ↓
loot/reward
        ↓
UI/journal/tooltip presentation
        ↓
persistence/save-reload
        ↓
regression/build verification
```

A partial demo does not close the era.

Examples:

- “the JSON loads” is insufficient;
- “the model renders” is insufficient;
- “the mob drops an item” is insufficient;
- “the quest completes” is insufficient.

The purpose is to prove the **connected production factory**.

---

# 2. Golden package rules

The golden package is **developer-only proof content**.

It must not silently become canon first-province content.

Use clearly non-production IDs, for example:

```text
world_rpg:dev/golden/...
```

or another explicit development namespace/prefix accepted by the registry rules.

Requirements:

- easy to delete/replace;
- no lore commitment;
- no balance authority;
- no final visual-style authority beyond proving the pipeline;
- every cross-domain reference visible and inspectable;
- intentionally small enough to rebuild repeatedly while the factory changes.

---

# 3. Golden package minimum content graph

The package should contain one small connected chain rather than unrelated samples.

## World

One developer POI / spawn anchor in a disposable test world or dev room.

It must have:

- stable POI/location identity;
- one spawn area or explicit actor placement;
- enough world metadata for the content to reference the place;
- deterministic inspection/debug output.

## NPC

One simple developer NPC or interaction source.

Needs:

- stable actor definition ID;
- display/presentation reference;
- dialogue/interaction reference;
- optional quest start/turn-in role;
- no advanced AI required unless needed for the graph.

## Enemy

One golden enemy.

Needs:

- mob definition;
- actor stats/resources;
- one production combat ability using real effect primitives;
- simple AI behavior;
- spawn/world reference;
- loot-table reference;
- presentation reference;
- physical Minecraft entity representation.

## Quest

One very small tracked developer quest.

Shape can be:

```text
NPC starts quest
-> defeat/interact with golden enemy
-> obtain/observe proof state
-> return to NPC
-> receive golden reward
```

The fiction does not matter.

The quest exists to prove:

- stable quest ID;
- state persistence;
- objective updates;
- dialogue/journal presentation;
- actor/world/item references;
- reward application;
- save/reload continuity.

## Item/reward

One golden item.

Needs:

- stable item ID;
- basic stats or one simple reusable effect;
- icon;
- optional simple model assignment;
- tooltip;
- loot/quest source;
- sell/buy/repair metadata only if the chosen item family requires them;
- persistence through inventory/save.

The item should be mechanically inspectable without creating a new one-off runtime class.

## Model / texture

One editable source model path proving:

- source master location;
- runtime export convention;
- stable asset ID;
- texture reference;
- scale/attachment convention;
- missing-export detection.

If the golden enemy uses a vanilla-compatible actor for the first iteration, the golden package still needs at least one custom model/texture asset elsewhere in the chain so the model pipeline is actually tested before Era 3 exits.

## Animation

Minimum proof:

- idle or locomotion;
- attack/cast/special action;
- hit/death or another state transition.

The point is not animation richness.

The point is proving source -> export -> runtime state mapping.

## VFX

At least one data-driven presentation effect referenced by gameplay content.

It should prove:

- stable presentation/VFX ID;
- source/reference validation;
- attachment/position rules;
- timing with the underlying gameplay event;
- no gameplay math embedded inside the VFX definition.

## Audio

At least one event-driven sound reference.

It should prove:

- stable audio/event ID;
- source/export/package path;
- gameplay/presentation event mapping;
- volume/category convention.

## UI

At minimum the golden chain must appear through real reusable UI plumbing:

- item tooltip/icon;
- quest/journal state;
- NPC dialogue/interact surface or production-intent equivalent;
- combat feedback necessary to understand the enemy ability.

Debug chat alone does not close the UI part of the factory.

---

# 4. Bootstrap gates

## F0 — physical foundation confidence

Precondition:

- sufficient P3 Minecraft evidence to trust entity/state/authority boundaries;
- first-province topology still allowed to remain graybox/provisional;
- no real gate failures hidden by developer helpers.

F0 does not require every later gameplay system to exist.

## F1 — content-domain source layout

Establish concrete source conventions for the first factory domains:

- items;
- loot;
- mobs;
- NPCs;
- quests;
- dialogue;
- zones/POIs;
- spawn groups;
- presentation references.

Do not introduce all future domains at once if the golden package does not use them.

Professions/factions/sets can follow once their first real package requires them.

Exit:

- each enabled domain has stable ID rules;
- source layout documented;
- strict decoding exists;
- malformed source fails loudly.

## F2 — semantic/cross-reference validation

The factory must reject at least:

- duplicate IDs;
- unknown references;
- unknown stat/effect/resource IDs;
- invalid loot references/weights;
- impossible quest prerequisites or mandatory circular dependencies where detectable;
- unknown actor/world/presentation IDs;
- invalid numerical ranges;
- required missing assets.

Validation errors identify source file + semantic path where practical.

Exit:

Deliberately break each golden-package reference type and prove the build/tool reports the failure usefully.

## F3 — deterministic compile/package

Where source format differs from runtime format, compilation/export must be:

- deterministic;
- stable-order;
- reviewable;
- reproducible in CI where practical.

Generated output is disposable.

Deleting `generated/` must not delete editable source truth.

## F4 — asset-source/export pipeline

Prove at least:

- model master -> runtime model;
- texture master/source -> runtime texture;
- animation source -> runtime animation data or mapped runtime state;
- icon master/source -> runtime icon;
- one VFX source/definition path;
- one audio master/event path.

Decide naming/versioning conventions before producing large asset volume.

Exit includes broken/missing asset detection.

## F5 — world-source pipeline

Prove:

- semantic POI identity;
- one spawn/placement definition;
- runtime mapping to physical world coordinates/entity placement;
- inspection/debug command or tool;
- versioning strategy for world metadata.

Do not solve the entire continent here.

## F6 — reusable UI path

Choose/prove the client UI approach using real golden-package data.

Minimum reusable primitives should cover:

- panel/container;
- text/typography;
- icon;
- tooltip;
- button/interaction;
- list/scroll where required;
- data binding/read-only presentation from authoritative state.

Do not build the complete UI system before proving one connected flow.

## F7 — golden package integrated runtime

Run the complete package in Minecraft.

Required evidence:

1. golden NPC/interaction exists at correct world source/POI;
2. quest starts through intended interaction;
3. journal/UI shows it;
4. golden enemy physically spawns/exists;
5. enemy uses actual combat runtime;
6. model/animation/presentation path is visible;
7. VFX/SFX fires from the correct event;
8. objective progresses authoritatively;
9. loot/item reward resolves;
10. item icon/tooltip appears through reusable UI;
11. save/reload preserves appropriate quest/item state;
12. broken reference tests fail correctly in factory/CI.

## F8 — throughput/revision test

The factory is not proven by building the golden package once.

Change it deliberately:

- change enemy ability reference;
- change item stat/effect;
- replace texture/model export;
- alter loot source;
- change quest objective text;
- move world placement.

Measure whether the iteration path is understandable and whether stale generated output causes problems.

If a tiny content change requires hunting through Java/runtime code, the factory is not ready for province volume.

---

# 5. Repository source layout — provisional

Do not create every directory until its domain has an accepted first schema.

Likely eventual shape:

```text
content/
  abilities/
  auras/
  items/
  loot/
  mobs/
  npcs/
  quests/
  dialogue/
  factions/
  professions/
  zones/
  spawn-groups/
  travel/
  presentation/

assets-source/
  models/
  textures/
  animations/
  icons/
  ui/
  vfx/
  audio/

world-source/
  regions/
  pois/
  routes/
  spawns/
  graveyards/
  transport/
  package-metadata/

tools/
  content-validation/
  content-compile/
  asset-validation/
  asset-export/
  world-validation/
  reports/
```

This is a planning layout, not permission to create empty folder theater.

Directories appear when their first real source files/tools appear.

---

# 6. Asset ID and presentation boundary

Gameplay definitions should reference stable presentation identities rather than hard-coded raw resource paths throughout gameplay code.

Desired direction:

```text
gameplay ability/item/mob
    -> presentation ID / asset-set ID
        -> model / animation / VFX / sound / icon references
```

This allows presentation revision without changing gameplay math.

However, avoid creating a giant abstract presentation framework before the golden package tells us what fields are actually required.

---

# 7. Authoring ergonomics requirements

The future content volume makes authoring experience a production feature.

Before first-province mass content, the factory needs:

- readable validation errors;
- ID/reference inspection;
- source file + semantic path in diagnostics;
- deterministic ordering;
- easy grep/search conventions;
- batch reports;
- duplicate/missing reference checks;
- a way to inspect what content actually loaded;
- one-command or normal build integration for validation;
- no requirement to edit generated output manually.

Nice-to-have after evidence justifies it:

- editor UI;
- graph visualizers;
- world placement visualization;
- loot/economy reports;
- spawn ecology maps;
- content-diff reports.

Do not build editor applications merely because the project is large. Build them when the authoring bottleneck is measured.

---

# 8. World binary/storage decision

Before production terrain begins, decide how large fixed-world artifacts are stored/versioned.

Potential considerations:

- Git repository size;
- Git LFS or equivalent;
- external retained source package;
- generated versus authoritative world artifacts;
- CI access;
- release packaging;
- backups;
- migration/version metadata.

This decision is not required for the tiny golden dev POI.

It **is** required before committing the first province's production world data.

---

# 9. What not to do in Era 3

Do not:

- author 100+ equipment definitions before the item/loot factory is proven;
- build the complete quest editor before one quest graph survives save/reload;
- make ten final creature models before one model/animation export loop is stable;
- produce the first province's final terrain before world-source/versioning is decided;
- create a universal VFX framework from imagination;
- build all 10-14 slice UI screens before reusable primitives have been proven with real data;
- add one Java class per content entry;
- hand-edit generated runtime files as source truth;
- hide validation problems behind warnings because 'we can fix them later';
- let the golden package become canon merely because it is first.

---

# 10. Measured outputs from Era 3

When the golden package works, record real throughput evidence:

- time/steps to author a new item using existing patterns;
- time/steps to create/export/integrate one model variant;
- time/steps to add one animation;
- time/steps to add one quest objective/reference;
- time/steps to place one spawn/POI;
- iteration friction after changing a cross-reference;
- asset validation failures encountered;
- number of manual steps not represented in source control/tooling;
- build/test cost;
- runtime reload/iteration cost.

Do not extrapolate the full game from these numbers yet.

Use them to replace the low-confidence portions of `FIRST_PROVINCE_PRODUCTION_LEDGER.md` with better estimates.

---

# 11. Era 3 handoff to first-province pre-production

After the factory is proven:

1. physically validate province topology;
2. choose the actual first-slice class set;
3. choose enemy-family roster;
4. outline quest-chain graph;
5. identify settlement/service NPC roster;
6. decide profession representation;
7. produce item/reward budget;
8. produce model/animation/VFX/SFX/icon ledger;
9. produce required UI surface list;
10. produce world architecture/environment kit list;
11. convert production-ledger ranges into named backlog items;
12. begin the first province production wave.

The golden package proves **how to make content**.

Era 4 decides **which content to make**.
