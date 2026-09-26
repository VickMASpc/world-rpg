# World RPG Development Cycle Contract

Status: ACTIVE OPERATING RULE

This document defines what counts as a meaningful development cycle.

A development cycle is not a single feature, registry, screen, model, command, content definition, test harness, or architectural abstraction.

A cycle must materially change the playable game across multiple connected production layers.

The phrase **"proceed development"** means: continue until the repository reaches a coherent integrated checkpoint with meaningful work in every mandatory lane below, unless a real blocker is discovered and recorded.

---

# 1. Mandatory lanes for every development cycle

Every cycle must include meaningful progress in all five lanes.

## A. Runtime / architecture

The cycle must deepen a production runtime boundary rather than only add one-off glue.

Examples:

- combat authority;
- character progression;
- equipment/stat ownership;
- NPC/dialogue state;
- economy/service runtime;
- spawn/ecology runtime;
- world-state/discovery;
- client/server synchronization;
- persistence/migration.

A small adapter used only to demonstrate another lane does not satisfy this requirement by itself.

## B. Authored content / game data

Every cycle must add a connected set of authored game content using the runtime being developed.

Examples:

- an enemy family rather than one isolated target;
- a quest chain rather than one debug objective;
- a useful equipment/reward set rather than a token item;
- service NPCs and offers;
- a real route/POI package;
- a coherent starter ability kit.

Content may remain provisional while production systems are still stabilizing, but it must be substantial enough to expose authoring and balancing problems.

## C. Player-facing UI / presentation

Every cycle must make the new systems understandable and usable without relying only on developer chat commands.

Depending on the cycle this may include:

- HUD/player/target/resource frames;
- action bars;
- cast feedback;
- quest journal/tracker;
- dialogue;
- bag/equipment;
- vendor/trainer/repair;
- XP/level feedback;
- loot feedback;
- map/discovery;
- death/recovery;
- tooltips/icons;
- animation/VFX/audio.

A single model or animation is presentation work, not an entire cycle.

## D. World / lived integration

The cycle must place its systems into actual Minecraft play.

Examples:

- authored spawn ecology;
- a measured route;
- a settlement service loop;
- a ruin/cave;
- a refuge;
- a danger pocket;
- an expedition path;
- world discovery;
- return-to-town behavior.

A developer room alone is acceptable only while the production path is impossible to test in a normal playable environment. It must not become the default development destination.

## E. Physical acceptance

The result must be physically playable and tested in Minecraft.

CI and unit tests remain mandatory but insufficient.

The acceptance evidence must answer:

- what can the player now do that they could not do before;
- what persistent state changes;
- what UI/presentation communicates it;
- what authored content exercises it;
- what world context it lives in;
- what failed or felt wrong during the run.

---

# 2. What does NOT count as a development cycle

The following can be subtasks or commits inside a cycle, but cannot be treated as the cycle's meaningful result:

- rendering one custom model;
- adding one animation controller;
- adding one registry/schema;
- adding one command;
- adding one quest;
- adding one item;
- adding one enemy;
- adding one UI screen with no integrated gameplay;
- adding a simulator fixture;
- adding tests/docs only;
- fixing one isolated bug unless it unblocks the larger integrated cycle;
- adding VFX/SFX without gameplay integration;
- refactoring architecture without a player-visible consequence.

These tasks are expected to occur routinely inside larger cycles.

---

# 3. Cycle completion rule

A cycle closes only when it has a playable sentence.

Examples of valid completion sentences:

> The player leaves Home A, tracks and fights an authored enemy family using World RPG combat, reads target/resource/cast state from the real HUD, receives RPG XP and loot, advances a real quest objective, and returns with persistent progression.

> The player returns from an expedition, opens a real bag/equipment screen, equips a meaningful upgrade that changes RPG stats, sells unwanted loot, repairs damaged gear, buys/trains an ability, and sees the resulting level/resource/toolkit change persist.

> The player follows a physically measured route, discovers a refuge and side ruin, speaks through real dialogue UI, keeps a multi-stage quest across several outings, encounters geographically authored spawn groups, and can navigate the route without developer commands.

If the cycle cannot be summarized in this form, it probably closed too early.

---

# 4. Replanned near-term production cycles

## Cycle 1 — First real combat expedition

### Architecture

- connect authored mobs to the production World RPG combat actor/runtime rather than vanilla health/damage as authority;
- establish persistent/player RPG combat state needed for real play;
- implement enemy combat state ownership, death resolution, aggro/leash/assist baseline and authored spawn-group ownership;
- establish the minimum synchronization path required by combat UI.

### Content

- convert Ashwood Wolf from single golden target into the first small enemy family/package;
- at least two behavior/stat variants using shared production patterns;
- real ability loadout including bite plus one non-trivial authored action;
- real loot table with useful material/equipment outcomes;
- one short quest/rumor chain that references the family;
- one physically authored hunting/spawn area.

### UI / presentation

- first production-intent target frame;
- player health/resource frame;
- cast/progress feedback where needed;
- minimal action bar/input surface for the player abilities used in the test;
- combat/loot feedback that does not depend on chat;
- Ashwood Wolf custom model/texture and idle/walk/bite animation path;
- first useful combat VFX/SFX hooks.

### World

- replace command-only spawning with a semantic spawn area;
- place the enemy package on a small real expedition route attached to the current graybox;
- baseline leash/respawn/population behavior;
- route must contain enough approach/travel context that combat is not a room test.

### Physical exit

The player can leave a safe point, encounter the authored Wolf package naturally, fight using World RPG combat, read the fight from actual UI, kill/loot it, advance authored progression, and return with state preserved.

**Custom Wolf rendering is one subtask in this cycle, not its milestone.**

---

## Cycle 2 — Character growth and civilization loop

### Architecture

- real character XP/level state;
- item equipment slots and equipment-derived RPG stats;
- equip/unequip mutation authority;
- durability/repair baseline if retained;
- merchant, trainer and repair service transaction boundaries;
- persistence/network state required by the player-facing surfaces.

### Content

- one coherent starter class/toolkit through several early unlocks;
- a starter equipment package plus several meaningful upgrades;
- merchant stock;
- trainer offers;
- repair economics;
- rewards from Cycle 1 connected to progression/equipment;
- Home A service NPC package.

### UI / presentation

- XP/level feedback;
- real bag interaction;
- character/equipment screen;
- item tooltips with stat comparison;
- vendor UI;
- trainer UI;
- repair feedback;
- spell/ability presentation for the starter toolkit.

### World

- Home Settlement A becomes a functional graybox settlement rather than a coordinate;
- inn/service area, merchant, trainer and repair roles are physically placed;
- expedition-return flow from Cycle 1 terminates here.

### Physical exit

The player can return from the wilderness, inspect/sell/equip loot, repair, train, level, alter their effective RPG stats/toolkit, and leave town with a materially changed character.

---

## Cycle 3 — First real quest-and-world expedition

### Architecture

- dialogue graph/data runtime;
- richer objective families: defeat, acquire, interact, discover plus existing visit/speak;
- quest event integration with mob deaths, inventory acquisition and world interactions;
- location discovery and semantic POI state;
- spawn-group/patrol/world-source definitions become first-class authored data.

### Content

- first substantial multi-stage quest chain designed to survive multiple outings;
- at least one side rumor/optional discovery;
- a second enemy family;
- one rare/elite package;
- one side ruin/cave;
- refuge NPCs and services sufficient to affect expedition reach;
- meaningful rewards tied into Cycle 2 equipment/progression.

### UI / presentation

- real dialogue screen;
- real quest journal;
- compact quest tracker;
- objective/POI discovery feedback;
- map/discovery primitives needed for the route;
- dialogue portraits/iconography only where justified by the chosen presentation direction.

### World

- physically develop the A -> route fork -> danger pocket -> Refuge F corridor;
- measure walking time;
- create at least one meaningful route decision/shortcut;
- place the side ruin/cave and early visible high-level danger;
- validate that the player can remember and re-traverse the route.

### Physical exit

The player can take a real quest through dialogue, leave civilization, navigate a meaningful route, fight/discover/interact through several objective types, use a refuge, preserve the quest across sessions, and return with remembered geography and rewards.

---

## Cycle 4 — First 1-3 hour World RPG micro-slice

This is the first integration wave where the game must begin feeling like World RPG rather than a collection of production proofs.

### Architecture

- death/recovery and graveyard behavior;
- stable save/reload across all prior systems;
- basic world-state consequences and respawn/reset policies;
- performance and migration hardening exposed by actual play.

### Content

- combine prior packages into a coherent 1-3 hour opening;
- several quest threads rather than a linear conveyor;
- meaningful quiet/travel time;
- multiple enemy/encounter packages;
- one memorable elite or deep-space encounter;
- enough equipment/reward history for item choices to matter;
- representative gathering/profession touchpoint if the earlier loops are stable.

### UI / presentation

- unify visual language across HUD, dialogue, journal, bag/equipment and services;
- first icon family;
- coherent combat and environment VFX/SFX;
- death/recovery presentation;
- remove obvious debug/chat dependencies from the normal path.

### World

- Home A and Refuge F connected through a believable opening region;
- dangerous pocket and side space;
- several landmarks;
- measured travel edges;
- sufficient authored environmental identity to test mental-map formation.

### Physical exit

A fresh player can play for roughly 1-3 hours without developer intervention and experience preparation, departure, travel, combat, questing, loot, progression, refuge/town return, character improvement and persistent unfinished reasons to go back out.

---

# 5. Operating cadence

Future development should not stop because one mandatory lane has reached a green CI checkpoint.

The normal cadence is:

```text
architecture work
    + content using it
    + UI/presentation
    + world integration
    + automated verification
    + Minecraft physical run
        ↓
coherent integrated checkpoint
```

Several commits and several internal subtasks are expected inside one cycle.

When a library/dependency decision appears inside a cycle, evaluate and admit/reject it as part of delivering the cycle rather than promoting the dependency decision into its own pseudo-phase.

---

# 6. Relationship to Era 3 / Era 4

Era 3 remains the current production-factory era, but it is no longer interpreted as a sequence of tiny isolated proof loops.

The golden package is now treated as material inside Cycle 1-3, not as permission to spend an entire development cycle on each asset or subsystem.

Era 4 first-province pre-production should begin to freeze real backlog decisions as these cycles produce evidence. It does not need to wait for every Era 3 concern to be academically complete.

The project should increasingly develop **vertically across workstreams**, not horizontally one subsystem at a time.

The plant remains subordinate to the game.
