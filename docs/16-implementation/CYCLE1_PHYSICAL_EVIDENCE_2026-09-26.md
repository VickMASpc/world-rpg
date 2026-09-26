# Cycle 1 — First Real Combat Expedition — Physical Evidence

Date: 2026-09-26

Branch: `p4/math-simulator`

Accepted candidate head before evidence reconciliation:

`2c3655067b17436306fe24ecdd25f6da9479f670`

Latest candidate CI at physical acceptance: #238 PASS.

---

# Result

**CYCLE 1 PHYSICALLY ACCEPTED.**

The physical run proved the integrated playable sentence:

> The player leaves the safe route, finds an authored Ashwood population without commands, reads and uses World RPG combat through the real HUD/input path, fights an animated enemy family governed by authored AI/combat, earns authored loot, advances the Ashwood quest, returns to the Refuge Scout, and keeps the resulting RPG state after reload.

---

# Accepted evidence

The user confirmed the comprehensive Cycle 1 suite passed after the final integration defects were repaired.

## Adventure path

PASS:

- player-state reset;
- Warden quest acceptance;
- East Road checkpoint location entry;
- ordered Warden report then separate turn-in;
- cloak + 40 copper reward;
- Refuge Scout second-quest acceptance;
- Collapsed Waystation visit;
- ordered Scout report then separate turn-in;
- provisions + 55 copper reward;
- Pressure in the Ashwood acceptance after prerequisite completion;
- Ashwood Verge physical entry and first objective completion;
- journal progression from 0/3 to 1/3.

The supplied log directly showed the full path through Ashwood Verge entry.

## Defect discovered during physical evidence

Two integration failures appeared immediately after the Ashwood Verge visit:

1. `/rpgjournal` failed once the current objective became `DefeatMob`.
2. Ashwood spawn ecology did not activate for a Creative-mode tester.

Root causes:

- journal presentation handled VisitLocation and then blindly cast every other objective to SpeakToNpc;
- ecology population activation reused the combat-aggro player filter, incorrectly treating Creative players as if their presence should not activate world population.

Repairs:

- exhaustive quest-objective presentation now supports VisitLocation, SpeakToNpc and DefeatMob;
- ecology population observation is separate from aggro eligibility;
- Creative players activate nearby authored population but are not combat targets;
- ecology diagnostics expose bound location, activation range, population counts, respawn countdown and last spawn error;
- authored mob force-spawn command now tests the full RPG definition/runtime path rather than only the raw Minecraft entity shell.

Regression CI:

- #235 PASS — journal fix;
- #236 PASS — DefeatMob regression;
- #237 PASS — ecology policy split;
- #238 PASS — diagnostics / authored spawn surface.

## Remaining Cycle 1 suite

After those fixes, the user confirmed **everything else passes**.

This accepts:

### Presentation

- custom World RPG Ashwood model;
- correct texture;
- idle/walk locomotion;
- authored howl/bite action animation path;
- combat HUD;
- target HUD;
- combat feedback;
- loot feedback.

### Combat authority

- Field Strike uses the production combat path;
- target RPG health changes through World RPG combat;
- focus cost/regeneration;
- cooldown/GCD rejection;
- authored enemies do not use vanilla attack damage as authority;
- ordinary vanilla melee does not bypass authored RPG health;
- authored bite damage goes through World RPG combat;
- physical defeat occurs from canonical RPG defeat.

### Enemy behavior / ecology

- natural authored population;
- Ashwood Wolf and Ashwood Stalker presence;
- aggro;
- pursuit;
- pack assistance;
- leash behavior;
- delayed population replacement;
- no runaway population multiplication.

### Adventure integration

- ordinary Wolf death gives loot without satisfying the Stalker objective;
- Stalker defeat advances exactly the authored defeat objective;
- Scout report and turn-in remain separate;
- reward is granted once;
- production Ashwood loot uses production item IDs rather than dev/golden IDs.

### Persistence / lifecycle

- quest state survives reload;
- bag/copper survives reload;
- adventure bindings survive reload;
- surviving authored mob/spawn-group bindings survive reload;
- dead mobs do not duplicate loot/quest credit after reload.

---

# Cycle 1 closure

Cycle 1 is no longer described as an implementation candidate.

It is the first physically accepted cross-domain production milestone.

It proves one connected chain:

```text
authored content
    -> semantic world location
    -> authored population
    -> custom creature presentation
    -> server-owned RPG combat
    -> player HUD/input
    -> loot
    -> quest progression
    -> persistent RPG state
```

The Ashwood package remains provisional First Province content. The acceptance proves the production path, not final balance, geography, art direction or content scale.

---

# Next milestone

**Cycle 2 — Character Growth and Civilization Loop**

The first bounded packet begins with persistent player character authority:

- XP / level;
- equipment slots;
- equip/unequip authority;
- equipment-derived combat stats;
- authored equipment definitions using those systems;
- a player-facing character/status proof surface.

Later Cycle 2 packets add interactive bag/equipment UI and settlement services on top of that authority.
