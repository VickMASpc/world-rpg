# Cycle 2 Packet 1 — Character / Equipment Authority Acceptance

Status: PHYSICAL EVIDENCE REQUIRED

This suite verifies only Packet 1.

Do not use it to claim the character/equipment UI or civilization loop is complete.

---

# A. Build

```powershell
git switch p4/math-simulator
git pull --ff-only origin p4/math-simulator
.\gradlew.bat build
```

Expected: BUILD SUCCESSFUL.

---

# B. Baseline character state

Run:

```text
/rpgcharacter
```

For a player with no prior `rpg_character` record:

- level 1;
- XP 0/300;
- max RPG HP 100;
- attack power 14;
- baseline level-1 armor;
- equipment slots empty unless equipment was already persisted.

---

# C. Existing cloak becomes real equipment

A Cycle 1 character that completed the first quest should already own:

`world_rpg:item/first_province/road_worn_cloak`

Run:

```text
/rpgbag
```

Expected cloak presentation includes:

- EQUIPMENT;
- BACK;
- `world_rpg:stat/armor +4.0`.

Then:

```text
/rpgcharacter equip world_rpg:item/first_province/road_worn_cloak
/rpgcharacter
```

Expected:

- BACK contains Road-Worn Cloak;
- armor increases by exactly 4;
- no level/XP change.

---

# D. Ownership validation

For an item not owned, equip must reject.

Deterministic setup is available through the operator proof command:

```text
/worldrpg inventory grant world_rpg:item/first_province/ashwood_carved_charm 1
```

Before granting, attempting to equip an unowned item must report that it is not owned.

---

# E. Required-level validation

Ashwood-Carved Charm requires level 2.

At level 1, after granting it to the bag:

```text
/rpgcharacter equip world_rpg:item/first_province/ashwood_carved_charm
```

Expected:

- rejected;
- reports required level 2;
- NECK remains empty;
- attack power unchanged.

---

# F. Level progression and live combat refresh

At a clean level-1 / 0 XP state:

```text
/worldrpg character grantxp 300
/rpgcharacter
```

Expected:

- level 2;
- XP 0/510;
- max RPG HP 112;
- base attack power increases from 14 to 16;
- level-derived armor increases;
- any equipped cloak modifier remains applied on top.

The combat HUD/server snapshot should reflect level 2 without reconnecting.

---

# G. Equip level-2 upgrade

At level 2 with the granted charm:

```text
/rpgcharacter equip world_rpg:item/first_province/ashwood_carved_charm
/rpgcharacter
```

Expected:

- NECK contains Ashwood-Carved Charm;
- attack power increases by exactly 3 above the level-2 baseline;
- with current provisional baseline, AP should be 19.

---

# H. Unequip authority

```text
/rpgcharacter unequip neck
/rpgcharacter
```

Expected:

- NECK becomes empty;
- attack power drops by exactly 3;
- item remains owned in the RPG bag.

Equipment is currently an equipped reference into persistent RPG inventory; equipping does not consume the bag stack.

---

# I. Authored kill XP

Fight one naturally authored Ashwood Wolf.

Expected loot notice includes:

```text
+24 XP
```

Fight one Ashwood Stalker.

Expected:

```text
+48 XP
```

When an authored kill crosses a level threshold:

- loot notice includes `LEVEL <n>`;
- live combat HP/AP/level refresh immediately;
- no reconnect required.

---

# J. Reload durability

With cloak/charm equipped and non-zero XP progress:

1. record `/rpgcharacter`;
2. fully exit the world;
3. reopen the same save;
4. run `/rpgcharacter` again.

Expected unchanged:

- level;
- XP within level;
- equipped item IDs/slots;
- derived equipment modifiers;
- resulting max HP/AP/armor.

---

# K. Reset coherence

Run:

```text
/worldrpg persistence resetplayer confirm
/rpgcharacter
```

Expected immediately, without reconnect:

- level 1;
- XP 0/300;
- equipment empty;
- max HP 100;
- AP 14;
- no old equipment modifiers.

The reset also clears quest/inventory/player persistence as before.

---

# Packet result

Packet 1 passes only if persistence and the live production combat actor agree throughout equip, level-up, reload and reset.
