# Cycle 1 — First Real Combat Expedition — Work Packets

Status: ACTIVE EXECUTION PLAN

Cycle 1 remains the meaningful integrated milestone.

This document splits that cycle into bounded work packets so development can proceed in pieces without lowering the project bar or losing cross-domain direction.

A packet is **not** a milestone by itself.

A packet exists to create a clean development stop, a focused CI boundary, and a precise next instruction.

---

# Current candidate state

The branch already contains a broad Cycle 1 candidate across:

- production combat resolution for authored abilities;
- production combat actor state for players and authored mobs;
- authored Ashwood Wolf / Ashwood Stalker family;
- authored bite/howl abilities and pack-fury aura;
- authored spawn-group domain and population runtime;
- aggro, assist, pursuit, leash, delayed respawn;
- authored defeat-mob quest objective routing;
- Ashwood Verge side-route graybox;
- first-pass player/target combat HUD;
- G-key Field Strike production combat input;
- networked combat-state and loot feedback;
- dedicated World RPG Ashwood creature entity;
- GeckoLib renderer/model/animation resources;
- canonical editable Ashwood Wolf source and export tooling;
- production Ashwood Fang / Pelt loot;
- Cycle 1 physical acceptance runbook.

The implementation is therefore already a candidate.

The remaining work is no longer one amorphous "finish Cycle 1" task. It is divided below.

---

# Packet 1 — Stabilize the current integrated candidate

## Goal

Freeze the current multi-system branch into a trustworthy build before adding more behavior.

## Work

- allow current CI to settle;
- fix compile/test/resource/export regressions only;
- verify the canonical Ashwood source exporter produces committed runtime assets without drift;
- verify the packaged combat-expedition content graph publishes;
- verify no production content depends on dev/golden item IDs;
- verify production combat networking/client classes are loaded by the correct environment source sets;
- verify GeckoLib is bundled/declared correctly for the produced mod JAR.

## Explicit non-goals

- no balance tuning;
- no new abilities;
- no new mobs;
- no additional UI;
- no new world geometry.

## Exit

A clean build from HEAD is sufficient to move to Packet 2.

This packet does not claim physical acceptance.

---

# Packet 2 — Physical boot and presentation proof

## Goal

Prove that the current candidate actually boots as a client mod and that the new presentation path exists in-game.

## Physical proof

- rebuild/reload the disposable route;
- confirm Ashwood Verge exists and is bound;
- walk near the hunting ground;
- confirm authored population appears without a spawn command;
- confirm the entities use the custom World RPG Ashwood model, texture, and animations;
- confirm no missing-resource/checkerboard/T-pose failure;
- confirm idle and pursuit/walk animation;
- confirm howl and bite action animations trigger from authored combat events;
- confirm first-pass combat HUD renders.

## Defect policy

Any boot, renderer, resource, animation, environment-loading, entity-registration, or HUD crash belongs to this packet and is fixed before moving on.

## Exit

The game boots and the creature/HUD/world candidate is physically visible and stable.

Do **not** yet require the whole expedition to pass.

---

# Packet 3 — Combat authority and enemy behavior proof

## Goal

Prove ordinary play is actually using World RPG combat authority rather than a vanilla/RPG hybrid.

## Work / proof

- Field Strike targets authored enemies through the production packet path;
- P4 production resolver changes authored RPG health;
- HUD reflects target RPG health and player focus;
- cooldown/GCD rejection is visible;
- focus regeneration works;
- normal vanilla melee cannot bypass authored enemy RPG health;
- authored enemy vanilla attack damage remains neutralized;
- bite damage reaches the player only through World RPG combat;
- aggro, pursuit, pack assist, and leash are physically visible;
- defeat bridge kills the physical entity exactly once.

## Repair scope

Fix authority leaks at their owning layer:

- combat state;
- event processing;
- target validation;
- vanilla health bridge;
- entity invulnerability;
- AI scheduling;
- network state.

## Exit

One Wolf and one Stalker can be fought end-to-end with no vanilla/RPG double-damage path.

---

# Packet 4 — Expedition content loop proof

## Goal

Prove combat participates in the adventure game rather than existing as a combat sandbox.

## Work / proof

Run the three-quest chain through:

1. Disappearances Along the East Road;
2. Silence at the Waystation;
3. Pressure in the Ashwood.

For the Ashwood quest prove:

- visit Ashwood Verge;
- defeat ordinary Wolf -> loot, no Stalker quest credit;
- defeat Stalker -> exactly one defeat-objective credit;
- current objective becomes report to Scout;
- first Scout click reports only;
- second Scout click turns in;
- reward grants once;
- production bag contains production Ashwood Fang/Pelt IDs only.

## Repair scope

- quest-prerequisite priority;
- ordered objective routing;
- defeat event routing;
- loot attribution;
- duplicate reward/credit;
- content graph/reference errors.

## Exit

The combat expedition changes quest and bag state correctly from world entry through turn-in.

---

# Packet 5 — Population, persistence, and lifecycle proof

## Goal

Prove the new world systems survive time and reload rather than only one encounter.

## Work / proof

- Wolf target population tends toward 3;
- Stalker target population tends toward 1;
- ordinary ticking never multiplies above authored target;
- respawn delays are respected during the running session;
- distant player absence prevents blind population manufacture;
- living authored-mob UUID -> mob/spawn-group bindings survive reload;
- adventure bindings survive reload;
- quest history and bag/copper survive reload;
- dead mobs do not generate duplicate loot after reload.

## Explicitly provisional

Cycle 1 still allows:

- player combat health/focus/cooldowns to reset as session combat state;
- spawn respawn timers to restart their schedule after server reload;
- crosshair targeting rather than sticky MMO target selection.

Those become later architecture work unless physical evidence shows they must be solved now.

## Exit

The expedition world behaves coherently across a full close/reopen cycle.

---

# Packet 6 — Cycle 1 repair and polish pass

## Goal

Use physical evidence to convert the candidate into a genuinely playable Cycle 1 milestone.

This packet is evidence-driven. It does not invent unrelated features.

## Likely work

Depending on the run:

- combat numbers that make the fight obviously broken;
- HUD sizing/placement/readability;
- targeting usability;
- animation timing;
- pursuit/leash jitter;
- spawn placement failures;
- route readability;
- loot feedback;
- missing sound/VFX cue if the fight is unreadable without it;
- quest text/presentation defects;
- client/server desync;
- persistence edge cases.

## Bar

Fix what materially prevents the playable sentence:

> The player leaves the safe route, finds an authored Ashwood population without commands, reads and uses World RPG combat through the real HUD/input path, fights an animated enemy family governed by authored AI/combat, earns authored loot, advances the Ashwood quest, returns to the Refuge Scout, and keeps the resulting RPG state after reload.

## Exit

All required rows in `COMBAT_EXPEDITION_ACCEPTANCE.md` are physically evidenced or explicitly recorded as failed.

If failures remain, Cycle 1 remains open.

---

# After Cycle 1

Do not continue horizontally expanding Ashwood systems.

The next integrated milestone remains:

## Cycle 2 — Character growth and civilization loop

Its first packet should begin with the character-state authority required by:

- XP / level;
- equipment;
- stat derivation;
- bag interaction;
- merchant / trainer / repair transactions;
- the corresponding player-facing UI.

The exact Cycle 2 packet breakdown should be written only after Cycle 1 physical evidence is reconciled.

---

# Operating command convention

Future instructions may use:

- **"Proceed Packet 1"**
- **"Proceed Packet 2"**
- etc.

When the user says only **"proceed development"** while Cycle 1 is active, continue the earliest incomplete packet rather than opening a new workstream.

At the end of a packet:

- report the exact HEAD;
- report latest confirmed green CI;
- state what changed;
- state what remains;
- provide only the physical test relevant to that packet;
- stop cleanly.

This packet system exists to make development resumable after tool/chat failures without shrinking meaningful project milestones.
