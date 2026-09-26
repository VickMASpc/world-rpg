# Adventure Runtime Physical Evidence — 2026-09-26

## Candidate

Branch: `p4/math-simulator`

Pre-evidence documented head:

```text
a6d959e99f8ca0e36a46712f74feb6c0af2d7a6d
```

CI run #167 completed successfully for that head.

## Result

**Core generic adventure-runtime physical gate: PASS.**

The remaining explicit persistence-reload rows E1-E3 were not exercised in this run and remain pending.

## Evidence summary

### A1 — full RPG IDs parse

Observed:

```text
Quest world_rpg:quest/first_province/east_road_disappearances | NOT_ACTIVE objectives=0/2
world_rpg:item/first_province/road_worn_cloak | quantity=0
```

Later in the same supplied run:

```text
Quest world_rpg:quest/first_province/east_road_disappearances | COMPLETED objectives=2/2
world_rpg:item/first_province/road_worn_cloak | quantity=1
```

No Brigadier trailing-data error occurred.

**A1 PASS.**

The initial NOT_ACTIVE/0 and later COMPLETED/1 observations are recorded exactly as supplied; this evidence file does not infer why the player's persisted state differed between those checks.

### A2 — one physical Warden click is single-fire

Observed after completed state:

```text
Road Warden: Nothing new for you right now.
```

One physical interaction produced one line rather than the previously duplicated Warden response.

**A2 PASS.**

### B1/B2 — expanded fixture and persistent binding graph

Observed build:

```text
adventure slice built | quest1=Disappearances Along the East Road quest2=Silence at the Waystation origin=1,-61,8 checkpoint=49,-58,8 refuge=105,-61,8 waystation=161,-61,8 changedBlocks=748 | Road Warden and Refuge Scout are driven by the generic adventure runtime
```

Observed status:

```text
first playable slice | origin=1,-61,8 checkpoint=49,-58,8 wardenAvailable=true quest1=COMPLETED objectives1=2/2 refuge=105,-61,8 waystation=161,-61,8 scoutAvailable=true quest2=NOT_ACTIVE objectives2=0/2 copper=40 items=1 | adventure world runtime | npcBindings=2 locationBindings=3
```

This proves:

- Warden exists;
- Refuge Scout exists;
- checkpoint/refuge/waystation fixture exists;
- two NPC bindings are active;
- three location bindings are active;
- original completed quest and reward state survived physical fixture reset/rebuild.

**B1 PASS.**
**B2 PASS.**

### C1 — second quest accepts through the generic NPC router

Observed:

```text
Refuge Scout: The refuge scout has lost contact with the next roadside waystation. Follow the east road to the collapsed post, inspect it, and return with a report.
Quest accepted: Silence at the Waystation
```

**C1 PASS.**

### C2 — generic location entry advances quest #2

Observed:

```text
Entered RPG location: Collapsed Waystation
Objective complete [Silence at the Waystation]: inspect waystation
```

**C2 PASS.**

### C3 — report and turn-in are separate physical interactions

First return interaction:

```text
Refuge Scout: That's enough. We can settle this now.
Objective complete [Silence at the Waystation]: report to scout
Quest ready to turn in. Interact again.
```

Later interaction:

```text
Refuge Scout: Done. Take your payment.
Quest complete: Silence at the Waystation
Reward: Roadside Provisions x2, 55 copper
```

No same-click report + turn-in collapse occurred.

**C3 PASS.**

### C4 — second reward path

Observed:

```text
Quest complete: Silence at the Waystation
Reward: Roadside Provisions x2, 55 copper
```

No duplicate reward was shown in the supplied run.

**C4 PASS for live reward transition.**

An explicit post-reload duplicate-reward check remains part of E3.

### D1 — authored prerequisite lock

After player RPG state reset, Refuge Scout interaction produced:

```text
Refuge Scout: Finish what came before first.
Quest locked: Silence at the Waystation | requires: Disappearances Along the East Road
```

The Scout remained locked again after the checkpoint objective and again after the Warden report objective, proving the prerequisite required actual completion/turn-in rather than partial progress.

**D1 PASS.**

### D2 — ordered Warden progression and single-transition report boundary

Observed:

```text
Road Warden: The road warden has asked you to follow the east road, inspect an abandoned checkpoint where travelers stopped returning, and report what you find. This definition is a provisional playable-slice production package, not final first-province lore.
Quest accepted: Disappearances Along the East Road
```

Then physical checkpoint entry:

```text
Entered RPG location: East Road Abandoned Checkpoint
Objective complete [Disappearances Along the East Road]: inspect route
```

Then Warden report interaction:

```text
Road Warden: That's enough. We can settle this now.
Objective complete [Disappearances Along the East Road]: report to warden
Quest ready to turn in. Interact again.
```

Only a later interaction turned the quest in:

```text
Road Warden: Done. Take your payment.
Quest complete: Disappearances Along the East Road
Reward: Road-Worn Cloak x1, 40 copper
```

This proves the interaction duplication defect that previously crossed report + turn-in in one click is fixed in the tested runtime.

**D2 PASS.**

### D3 — prerequisite unlock after actual quest completion

Before Warden turn-in, repeated Scout interactions remained locked.

After Warden turn-in:

```text
Refuge Scout: The refuge scout has lost contact with the next roadside waystation. Follow the east road to the collapsed post, inspect it, and return with a report.
Quest accepted: Silence at the Waystation
```

The second quest then completed through the same generic location/NPC path:

```text
Entered RPG location: Collapsed Waystation
Objective complete [Silence at the Waystation]: inspect waystation
Refuge Scout: That's enough. We can settle this now.
Objective complete [Silence at the Waystation]: report to scout
Quest ready to turn in. Interact again.
Refuge Scout: Done. Take your payment.
Quest complete: Silence at the Waystation
Reward: Roadside Provisions x2, 55 copper
```

**D3 PASS.**

## Acceptance matrix after this run

| Gate | Result |
| --- | --- |
| A1 full RPG ID parsing | PASS |
| A2 one Warden click = one interaction | PASS |
| B1 expanded physical fixture | PASS |
| B2 2 NPC / 3 location bindings | PASS |
| C1 second quest generic NPC acceptance | PASS |
| C2 second quest generic location advance | PASS |
| C3 report and turn-in separated | PASS |
| C4 second reward live transition | PASS |
| D1 prerequisite lock | PASS |
| D2 ordered first quest + single transition | PASS |
| D3 prerequisite unlock after completion | PASS |
| E1 quest/reward persistence after full reload | PENDING |
| E2 physical binding persistence after full reload | PENDING |
| E3 post-reload interaction/reward idempotence | PENDING |

## Interpretation

This run is sufficient to establish the intended architectural result:

**the first playable quest is no longer special.**

Two authored quests now use the same reusable physical path:

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
```

The original duplicate-Warden defect is physically closed for live play.

The remaining persistence-reload rows are important durability checks, but they do not reopen the generic routing architecture proof that passed here.
