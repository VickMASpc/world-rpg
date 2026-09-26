# Cycle 2 — Character Growth and Civilization Loop — Work Packets

Status: ACTIVE EXECUTION PLAN

Cycle 2 is the second integrated production milestone.

Cycle 1 proved departure -> world -> combat -> loot -> quest -> persistence.

Cycle 2 must prove that returning with loot and experience materially changes the character and makes civilization mechanically important.

Packets are bounded implementation/testing stops. They are not milestones by themselves.

---

# Packet 1 — Persistent character and equipment authority

Status: IMPLEMENTED CANDIDATE

## Goal

Create the persistent player-character authority that later UI and settlement services will operate on.

## Architecture

- persistent World RPG character record;
- level 1-100 state;
- experience-within-level state;
- deterministic provisional XP curve;
- authored mob XP rewards;
- equipment slots;
- authored equipment slot/stat payloads on item definitions;
- equip/unequip authority;
- level requirements;
- owned-item requirement;
- equipment-derived combat modifiers;
- persistent level/equipment -> production combat stat refresh;
- resetplayer coherence between persistence and live combat state.

## Content

- Road-Worn Cloak is now real BACK equipment:
  - +4 armor;
- Ashwood-Carved Charm:
  - NECK;
  - required level 2;
  - +3 attack power;
  - 25% Ashwood Stalker drop;
- Ashwood Wolf:
  - 24 XP;
- Ashwood Stalker:
  - 48 XP.

## Player-facing proof surfaces

- `/rpgcharacter`
- `/rpgcharacter equip <item-id>`
- `/rpgcharacter unequip <slot>`
- RPG bag now prints equipment slot/stat payloads;
- combat loot notice includes XP and level-up text.

## Developer proof surfaces

- `/worldrpg character grantxp <amount>`
- `/worldrpg inventory grant <item-id> <quantity>`

## Exit

Packet 1 closes only when physical Minecraft evidence proves:

- level/XP persists;
- level changes live combat HP/AP;
- equipment changes live combat stats;
- equipment persists through reload;
- required-level and ownership checks reject invalid equips;
- resetplayer resets persistent and live character authority together;
- authored mob kills grant the authored XP amount.

---

# Packet 2 — Real bag / character / equipment UI

## Goal

Replace command proof surfaces with the first actual inventory/character interaction loop.

## Architecture

- server-authoritative inventory/equipment mutation packets;
- client character/inventory snapshot;
- item/equipment comparison model;
- slot interaction rules;
- equipped-count/bag presentation semantics.

## UI

- character paper-doll/equipment screen;
- interactive bag;
- item tooltips;
- required-level state;
- equipped/unequipped state;
- stat deltas on comparison;
- XP/level presentation;
- equipment changes reflected immediately in combat HUD state.

## Content

Use the cloak/charm and several additional early pieces to expose multiple slots and replacement decisions.

## Exit

The player can inspect bag -> compare -> equip -> replace -> unequip without commands and can see the resulting stat changes.

---

# Packet 3 — Home A service authority

## Goal

Make Home Settlement A mechanically useful.

## Architecture

- merchant offer definitions;
- buy/sell transaction authority;
- trainer offer/unlock authority;
- repair/durability authority if durability remains in the design;
- transaction validation and persistent state.

## Content / world

- merchant;
- trainer;
- repair/service role;
- physically placed Home A service NPCs;
- initial stock and trainer offers;
- prices tied to the opening economy.

## UI

- vendor surface;
- trainer surface;
- repair/service feedback.

## Exit

The player can return from the expedition, sell loot, buy a useful item, train a character improvement and leave with materially changed state.

---

# Packet 4 — Cycle 2 integrated return loop

## Goal

Physically prove the full civilization half of the RPG loop.

## Playable sentence

> The player returns from an expedition with XP and loot, sees persistent level progress, equips a meaningful upgrade, sells unwanted materials, spends money on a useful purchase/service, trains a character improvement, and leaves Home A with changed stats/toolkit that survive reload.

## Exit

Cycle 2 closes only after integrated physical acceptance.

---

# Operating rule

When Cycle 2 is active and the user says only "continue/proceed development", continue the earliest incomplete packet.

At each packet boundary:

- exact HEAD;
- latest green CI;
- implemented result;
- narrow physical suite;
- stop cleanly.
