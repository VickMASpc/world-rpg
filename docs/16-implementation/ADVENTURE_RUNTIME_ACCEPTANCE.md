# Adventure Runtime Expansion — Physical Acceptance Guide

## What this gate proves

This gate moves World RPG beyond a single hard-coded quest fixture.

The candidate must prove that:

- physical Minecraft entities are persistently bound to authored NPC IDs;
- physical world volumes are persistently bound to authored location IDs;
- one server-side NPC interaction causes at most one quest-state transition;
- quest objectives advance only in authored order;
- location-entry and NPC-interaction events route through one generic quest-event layer;
- quest prerequisites are authored data and are enforced by the quest runtime;
- a second quest can run through the same machinery without quest-specific Java progression code;
- players can inspect active quests and RPG rewards without operator-only developer commands.

## New player-facing commands

These commands do not require operator permission:

```text
/rpgjournal
/rpgbag
```

`/rpgjournal` shows active quest title, state, summary and current authored objective.

`/rpgbag` shows persistent RPG copper and resolves stored RPG item IDs to player-facing item names, categories, requirements and vendor values.

## New developer reset command

On a disposable test save:

```text
/worldrpg persistence resetplayer confirm
```

This clears only the current player's World RPG persisted record:

- active/completed quest state;
- RPG inventory and copper;
- developer player data.

It does not delete Minecraft inventory, world blocks, physical adventure bindings or the slice itself.

---

# A. Existing completed-world smoke test

This is the fastest validation for the bugs found during the original playable-slice run.

Update/build the branch, then load the same completed test world.

Run:

```text
/worldrpg quest status world_rpg:quest/first_province/east_road_disappearances
```

Expected:

```text
Quest world_rpg:quest/first_province/east_road_disappearances | COMPLETED objectives=2/2
```

Run:

```text
/worldrpg inventory inspect world_rpg:item/first_province/road_worn_cloak
```

Expected:

```text
world_rpg:item/first_province/road_worn_cloak | quantity=1
```

These full IDs must parse without the old “Expected whitespace...” failure.

Now right-click the Road Warden exactly once.

Expected:

- exactly one NPC response;
- no duplicate response;
- no inventory change;
- no quest-state change.

Then run:

```text
/rpgbag
```

Expected to include:

```text
RPG Bag | copper=40
Road-Worn Cloak x1
```

Run:

```text
/rpgjournal
```

If no other quest is active, expected:

```text
RPG Journal | no active quests | completed=1
```

A duplicate Warden response on one physical click fails this gate.

---

# B. Expand the old fixture without losing completed progression

The old first-slice world contains only the Warden/checkpoint physical fixture. The runtime will still migrate its old Warden/checkpoint bindings automatically, but the second NPC and eastern continuation require rebuilding the disposable fixture.

On the completed disposable world:

```text
/worldrpg slice reset confirm
/worldrpg slice build confirm
```

Player quest/inventory state is intentionally not cleared.

Expected physical result:

1. Road Warden at the western post.
2. East Road Abandoned Checkpoint around +48 X from the build origin.
3. East Road Refuge around +104 X.
4. Named **Refuge Scout** at the refuge.
5. Collapsed Waystation around +160 X.
6. One continuous dirt road connects the test sites.

Run:

```text
/worldrpg slice status
```

Because the original Warden quest was already completed, expected state includes roughly:

```text
wardenAvailable=true
quest1=COMPLETED
objectives1=2/2
scoutAvailable=true
quest2=NOT_ACTIVE
objectives2=0/2
npcBindings=2
locationBindings=3
```

Existing copper/item values should remain 40/1.

---

# C. Prove quest #2 uses the generic machinery

With quest #1 already completed, right-click the Refuge Scout once.

Expected:

- exactly one interaction response;
- `Silence at the Waystation` becomes active;
- no duplicate acceptance.

Run:

```text
/rpgjournal
```

Expected to include:

```text
Silence at the Waystation | ACTIVE | 0/2
Current objective: Visit Collapsed Waystation
```

Walk east to the Collapsed Waystation.

Expected automatically on entering the bound location volume:

- the location is identified as **Collapsed Waystation**;
- `inspect_waystation` completes;
- quest remains active at 1/2.

Run `/rpgjournal` again.

Expected current objective:

```text
Current objective: Speak to Refuge Scout
```

Return to the Refuge Scout.

## First return click

Right-click once.

Expected:

- exactly one NPC response;
- `report_to_scout` completes;
- quest becomes READY_TO_TURN_IN;
- **no reward is granted yet**.

## Second return click

Right-click again.

Expected:

- quest completes;
- reward grants exactly once:
  - Roadside Provisions x2
  - 55 copper.

Run:

```text
/rpgbag
```

If this is the same player that completed the original Warden quest, expected totals are:

```text
RPG Bag | copper=95 stacks=2 items=3
Road-Worn Cloak x1
Roadside Provisions x2
```

Repeated Scout interactions must not duplicate the reward.

---

# D. Full prerequisite + single-transition replay

This test can reuse the same expanded physical fixture.

Run:

```text
/worldrpg persistence resetplayer confirm
```

Expected:

```text
World RPG player state reset.
```

Confirm:

```text
/rpgbag
/rpgjournal
```

Expected:

- 0 copper;
- empty RPG bag;
- no active quests;
- completed=0.

## D1 — prerequisite lock

Go to the Refuge Scout before doing anything for the Road Warden.

Right-click once.

Expected:

```text
Refuge Scout: Finish what came before first.
Quest locked: Silence at the Waystation | requires: Disappearances Along the East Road
```

Expected state:

- quest #2 remains NOT_ACTIVE;
- no objective progress;
- no reward.

This proves the prerequisite belongs to authored quest data rather than physical-slice Java.

## D2 — ordered Warden quest

Right-click Road Warden once.

Expected quest #1 ACTIVE at 0/2.

Right-click him again while still at the post.

Expected:

- no later `speak_to_npc` objective completion;
- quest remains 0/2.

This specifically proves objective ordering. The report objective cannot be satisfied before the visit objective.

Walk to the checkpoint.

Expected quest #1 advances to 1/2.

Return to Warden and click once.

Expected:

- report objective completes;
- quest becomes READY_TO_TURN_IN;
- reward remains 0 copper / 0 items.

This click must not also turn the quest in.

Click the Warden a second time.

Expected:

- quest completes;
- Road-Worn Cloak x1;
- 40 copper.

## D3 — prerequisite unlock

Return to Refuge Scout and right-click once.

Expected:

- no lock message now;
- `Silence at the Waystation` is accepted.

Complete its visit/report/turn-in sequence as described in section C.

---

# E. Persistence of generic bindings

After both quests are complete:

1. exit the world fully;
2. reopen the same save;
3. load the slice area;
4. run:

```text
/worldrpg slice status
/rpgbag
```

Expected:

- Warden available;
- Scout available;
- generic runtime reports 2 NPC bindings and 3 location bindings;
- quest #1 completed 2/2;
- quest #2 completed 2/2;
- 95 copper;
- cloak x1;
- provisions x2.

Right-click each NPC once after reload.

Expected:

- one response per physical click;
- no quest mutation;
- no duplicate rewards.

---

# Recorded physical result — 2026-09-26

The live Minecraft run recorded in:

- `docs/16-implementation/ADVENTURE_RUNTIME_PHYSICAL_EVIDENCE_2026-09-26.md`

physically passed A1-A2, B1-B2, C1-C4 and D1-D3.

The explicit full-reload durability rows E1-E3 remain pending.

This means the reusable adventure-routing architecture is accepted as physically proven, while complete persistence-after-relaunch acceptance remains open.

---

# Acceptance matrix

| Gate | Pass condition |
| --- | --- |
| A1 | Full RPG IDs parse in quest/inventory commands |
| A2 | One Warden click produces one interaction only |
| B1 | Expanded fixture creates Warden, checkpoint, refuge, Scout and waystation |
| B2 | Runtime reports 2 persistent NPC bindings and 3 location bindings |
| C1 | Second quest accepts through Scout interaction |
| C2 | Waystation location entry automatically advances current objective |
| C3 | First return click reports; second return click turns in |
| C4 | Second reward grants exactly once |
| D1 | Scout quest is locked before Warden quest completion |
| D2 | Later speak objective cannot complete before earlier visit objective |
| D3 | Completing Warden quest unlocks Scout quest |
| E1 | Both quest histories and RPG bag survive reload |
| E2 | Physical NPC/location bindings survive reload |
| E3 | Post-reload NPC clicks remain single-fire |

This expansion is physically accepted when all applicable gates pass.

## Architectural meaning

Passing this gate proves that the first playable quest is no longer special.

The reusable path is now:

```text
Minecraft entity / world position
        ↓
persistent physical RPG binding
        ↓
AdventureWorldRuntime
        ↓
ordered AdventureQuestEventRouter
        ↓
authored QuestContentDefinition
        ↓
persistent quest + RPG inventory
        ↓
journal / bag player surfaces
```

The second quest exists to prove that another authored quest can use the same path without another quest-specific interaction state machine.
