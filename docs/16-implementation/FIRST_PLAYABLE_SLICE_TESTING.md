# First Playable Slice — Physical Acceptance Guide

## Purpose

This guide validates the first World RPG loop that can be played in Minecraft rather than driven by developer progression commands.

The slice is deliberately tiny and provisional. It is not the first-province map and it is not final quest writing. Its purpose is to prove one connected player-facing chain:

**physical NPC -> quest acceptance -> physical travel -> location objective -> NPC report -> turn-in -> persistent RPG reward**

The authoritative quest, NPC, location and item still come from the normal World RPG content registries. The physical slice refuses to build if that authored content contract no longer matches the runtime assumptions.

## Candidate branch

Use:

```text
p4/math-simulator
```

The candidate commit containing the physical slice is at or after:

```text
0127013ea5bba7093bcee0b0b06aaf8119abd75c
```

## Recommended test world

Use a **new disposable Creative Superflat world with cheats enabled**.

The slice can follow ordinary surface height, but Superflat removes terrain noise from the acceptance test and makes the 48-block road/checkpoint layout obvious.

Do not use an important survival world. The build command intentionally replaces surface blocks to create the test road/post/checkpoint, and slice reset does not restore those blocks.

## Build the mod

From the repository root:

### Windows PowerShell

```powershell
git switch p4/math-simulator
git pull
.\gradlew.bat build
```

### macOS / Linux

```bash
git switch p4/math-simulator
git pull
./gradlew build
```

Expected result:

- Gradle exits successfully.
- Tests pass.
- A development JAR is produced under `build/libs/`.

Install the generated World RPG JAR together with the required Fabric Loader/Fabric API versions for Minecraft 1.21.1.

## Test 0 — content runtime sanity

Enter the world and run:

```text
/worldrpg content status
```

Expected:

- World RPG reports an active published content snapshot.
- There are no load/validation errors.

The playable slice depends on these authored IDs resolving:

```text
world_rpg:npc/first_province/road_warden
world_rpg:location/first_province/east_road_checkpoint
world_rpg:quest/first_province/east_road_disappearances
world_rpg:item/first_province/road_worn_cloak
```

If content is not published, stop here. A physical slice should not be used to bypass a failed content snapshot.

## Test 1 — build the physical slice

Stand in an open part of the disposable Overworld and run:

```text
/worldrpg slice build confirm
```

Expected chat result begins with:

```text
first playable slice built
```

and includes:

- quest title
- origin coordinates
- checkpoint coordinates
- changed block count
- instruction to right-click the Road Warden

Expected world result:

- A named **Road Warden** villager appears at the start point.
- A small coarse-dirt post area exists around him.
- A visible dirt-path road runs roughly 48 blocks east (+X).
- The road ends in a gravel checkpoint.
- The checkpoint has a mossy-cobblestone marker and a campfire.

Run:

```text
/worldrpg slice status
```

Expected:

```text
wardenAvailable=true
quest=NOT_ACTIVE
objectives=0/2
copper=0
items=0
```

The exact origin/checkpoint coordinates depend on where the command was run.

### Negative check

Run the build command again without resetting:

```text
/worldrpg slice build confirm
```

Expected:

- build is rejected
- message states that a first playable slice already exists

This proves the world-persistent slice anchor is not silently duplicated.

## Test 2 — accept through a real NPC interaction

Do **not** use `/worldrpg quest accept`.

Right-click the Road Warden with the main hand.

Expected dialogue/result:

1. Warden describes the missing travelers and tells you to inspect the east-road checkpoint.
2. Chat reports:
   ```text
   Quest accepted: Disappearances Along the East Road
   ```
3. Chat gives the physical objective to follow the road east.

Run:

```text
/worldrpg slice status
```

Expected:

```text
quest=ACTIVE
objectives=0/2
copper=0
items=0
```

Optional cross-check:

```text
/worldrpg quest status world_rpg:quest/first_province/east_road_disappearances
```

Expected:

```text
ACTIVE objectives=0/2
```

### Repeat-interaction check

Right-click the Road Warden again before visiting the checkpoint.

Expected:

- quest does not duplicate
- objective count remains 0/2
- Warden reminds you that the checkpoint is east along the road

## Test 3 — advance by walking into the authored location

Walk the dirt road east. Do not use:

```text
/worldrpg quest advance ...
```

Enter the gravel checkpoint near the mossy marker/campfire.

Expected automatically, without a command:

```text
You inspect the abandoned checkpoint: cold ashes, discarded bedrolls, and tracks cutting away from the road.
Objective complete: inspect the east-road checkpoint.
New objective: return to the Road Warden and report what you found.
```

Run:

```text
/worldrpg slice status
```

Expected:

```text
quest=ACTIVE
objectives=1/2
```

Walking out and back into the checkpoint must **not** increment progress again or produce a second completion.

This is the central location proof: objective state is now driven by the player's physical position relative to the persisted slice checkpoint.

## Test 4 — report by returning to the NPC

Walk back along the road and right-click the Road Warden.

Expected:

- Warden responds to the evidence from the checkpoint.
- Chat reports:
  ```text
  Objective complete: report your findings to the Road Warden.
  Quest ready to turn in. Speak to the Road Warden again.
  ```

Run:

```text
/worldrpg slice status
```

Expected:

```text
quest=READY_TO_TURN_IN
objectives=2/2
copper=0
items=0
```

The reward must **not** be granted yet. Reporting and reward turn-in are deliberately separate interactions.

## Test 5 — turn in and receive persistent RPG rewards

Right-click the Road Warden one more time.

Expected:

```text
Quest complete: Disappearances Along the East Road
Reward: Road-Worn Cloak x1, 40 copper
RPG inventory: copper=40 totalItems=1
```

Run:

```text
/worldrpg slice status
```

Expected:

```text
quest=COMPLETED
objectives=2/2
copper=40
items=1
```

Cross-check the canonical RPG inventory:

```text
/worldrpg inventory status
/worldrpg inventory inspect world_rpg:item/first_province/road_worn_cloak
```

Expected:

```text
RPG inventory | copper=40 distinctItems=1 totalItems=1
world_rpg:item/first_province/road_worn_cloak | quantity=1
```

Right-clicking the Warden again must not duplicate the reward. He should give post-quest dialogue and the inventory values must remain unchanged.

## Test 6 — save/reload persistence

After completing Test 5:

1. Exit to the title screen.
2. Re-open the same world.
3. Return to the slice.
4. Run:

```text
/worldrpg slice status
```

Expected:

- the placed road/checkpoint remain because they are ordinary world blocks
- the Road Warden still exists
- `wardenAvailable=true` once his chunk is loaded
- quest remains `COMPLETED`
- objective count remains `2/2`
- copper remains `40`
- item count remains `1`

Then run the inventory inspect command again. The cloak quantity must still be 1.

This proves both the physical slice anchor and player RPG state survive the world save.

## Test 7 — anti-cheat-path confirmation

The milestone should be accepted only if Tests 2-5 were completed without the following developer progression commands:

```text
/worldrpg quest accept
/worldrpg quest advance
/worldrpg quest turnin
```

Those commands remain useful developer inspection/proof surfaces, but they do not count as the playable-loop evidence.

The only setup command allowed for this acceptance run is:

```text
/worldrpg slice build confirm
```

## Test 8 — slice reset behavior

On a disposable world, run:

```text
/worldrpg slice reset confirm
```

Expected:

- persisted physical slice anchor is removed
- Road Warden entity is removed if currently available
- the road/checkpoint blocks are intentionally left in place
- player quest/inventory progression is **not** erased

This command resets the physical test fixture, not the player's RPG history.

Because completed quests are intentionally persistent, use a new disposable world/player when repeating the full acceptance run from zero.

## Acceptance matrix

| Gate | Action | Expected result |
| --- | --- | --- |
| S1 | Build slice | Road Warden + physical road + checkpoint exist |
| S2 | Right-click Warden | Quest becomes ACTIVE, 0/2 |
| S3 | Walk into checkpoint | inspect_route completes automatically, 1/2 |
| S4 | Return/right-click Warden | report_to_warden completes, READY_TO_TURN_IN, 2/2 |
| S5 | Right-click Warden again | Quest COMPLETED; cloak x1 + 40 copper |
| S6 | Repeat Warden interaction | No duplicate reward |
| S7 | Save/reload world | NPC anchor, quest completion and rewards persist |
| S8 | No quest progression commands used | Entire player loop was physical |

The first playable-slice milestone is physically accepted only when **S1-S8 all pass**.

## Failure interpretation

### Build says the quest definition is not loaded

Run:

```text
/worldrpg content status
```

This is a content publication problem. Do not weaken the slice contract to bypass it.

### Build says starter/turn-in or objective target no longer matches

The authored JSON changed without updating the physical slice integration. This is an intentional hard failure. Reconcile the content/runtime contract.

### Checkpoint does not advance the quest

Verify:

- quest is ACTIVE
- you are at the generated checkpoint coordinates shown by `/worldrpg slice status`
- you are within roughly five horizontal blocks of the checkpoint center
- you are in the Overworld
- `inspect_route` is not already complete

### Warden does not respond

Run:

```text
/worldrpg slice status
```

If `wardenAvailable=false`, ensure the Warden's chunk is loaded. If the entity was manually killed/removed, reset the disposable slice and rebuild in a fresh test world.

### Reward is granted before the second return interaction

Fail the milestone. Reporting and turn-in are supposed to be separate.

### Reward can be duplicated

Fail the milestone. Completed quest history must block duplicate turn-in.

## What this milestone does not claim

Passing this guide does **not** mean the first province is production-ready.

It proves the first connected player-facing World RPG chain:

- authored content resolves
- a physical NPC represents that content
- the player accepts by interaction
- physical movement completes a location objective
- NPC interaction completes a conversation objective
- a separate turn-in grants canonical RPG rewards
- quest/inventory/world anchor state persists

Dialogue UI, final NPC models, journal UI, final equipment rendering, real economy UI, combat objectives, final terrain, animation, VFX and audio remain later production work.
