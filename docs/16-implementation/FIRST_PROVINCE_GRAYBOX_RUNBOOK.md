# First-province graybox walk-test runbook

Status: READY AFTER WP-002 BRANCH BUILD / CI PASS

This is a disposable physical topology test, not production terrain. Its geometry and route-time bands remain provisional until walked and interpreted by the Anchor.

## Prepare a safe test world

1. Build the WP-002 branch with the normal Minecraft 1.21.1 / Fabric development setup:

   ```text
   ./gradlew build
   ```

2. Create a new, dedicated **Superflat Overworld** test world. Do not use a production or personal world. The builder replaces surface blocks along its roads, river, bridge, and marker columns.
3. Enable commands / use a permission-level-2 operator. Stand on the intended home location; the command-time block position becomes A.
4. Run:

   ```text
   /worldrpg province graybox build confirm
   ```

   The builder rejects non-Overworld use and sampled uneven routes. It does not teleport the player or change RPG/player state. Coordinates use `+X east` and `+Z south`. Repeating it at the same origin is idempotent; running it at another origin creates a second layout and does not clear the first one.
5. After construction, use peaceful difficulty and Adventure or Survival. Walk on the ground only: do not sprint, fly, use speed effects, mount, or teleport during a timed leg. A phone stopwatch is sufficient.

If the command fails its flatness check, preserve the error and stop. Do not try it in a natural world or change the layout to force construction.

## Marker and route key

The command prints this key on completion:

| Marker | Node | Role |
| --- | --- | --- |
| Red | A | Home |
| Yellow | B | Local wild edge |
| Orange | C | Main corridor |
| White | D | River bridge and fork |
| Black | E | Early visible danger pocket |
| Light blue | F | Remote refuge |
| Purple | G | Regional settlement |
| Cyan | H | Deeper outward threshold |
| Lime | B2 | Work/resource detour |
| Green | R1 | Local ruin |
| Magenta | R2 | Unfinished dangerous site |

Road colors: light gray is the safe road; red is the shorter danger route; brown is the learned G-to-B return shortcut; lime marks local detours; cyan continues beyond F. These colors are construction aids, not final world presentation.

There are no NPCs, services, hostiles, resources, quests, or combat in this graybox. A/F/G are location markers only. The black and magenta markers do not implement or prove danger mechanics.

The provisional measurement bands are:

| Route | Target |
| --- | ---: |
| A -> B local wild edge | ~2-5 min |
| A -> R1 local ruin | ~6-12 min |
| A -> D safe road / bridge | ~10-18 min |
| D -> F safe route | ~8-15 min |
| A -> F safe route | ~20-30 min |
| A -> G safe first journey | ~25-40 min |
| F -> H outward threshold | ~15-30 min |

The builder uses a 250 blocks/minute reference solely to check rough geometric scale. The measured human walk is authoritative, and every band remains open to revision from that evidence.

## Minimal physical route batch

Run these after construction. Record each route separately; do not infer pass/fail from distance alone.

1. **Local familiarity:** A -> B, then A -> R1 along the green detour. Record the first walk and whether B/R1 feel local rather than immediate.
2. **Danger approach:** From the safe corridor, approach E along the red route, stop when the marker becomes legible, then retreat to the safe road. Record when E first became visible, whether retreat was obvious, and whether the safe/risk distinction read clearly.
3. **Regional first journey:** A -> G by following the light-gray safe road through B, C, and the white D bridge/fork. Record the first-time walk, wrong turns, map checks, and remembered landmarks.
4. **Learned return:** On the first A -> G journey, stay on the light-gray safe road and do not follow the brown shortcut. From G on the return, use the narrow brown route to B, then the safe road back to A. Record whether the shortcut feels like a newly learned route, whether it saves a meaningful amount of time, and whether it still feels like travel. The shortcut is visibly marked in this disposable graybox, so this tests its physical length/use after a first safe journey, not how final terrain would teach discovery.
5. **Remote reach:** A -> F on the safe road, then F -> H. At F, record whether turning back versus continuing toward H feels like a distinct expedition decision. This tests the refuge and outward-threshold spacing.
6. **Optional workland detour:** B -> B2 -> B. Record the time cost and whether the detour feels like a real choice from the main goal.

Do not populate the quiet legs with enemies or rewards. Record repetition, featurelessness, or navigation trouble as observed; do not automatically solve quiet time by shortening routes or adding encounters.

## Evidence sheet

Copy one block for each route:

```text
run id:
route:
first-time or known:
start/end:
walking time:
time spent checking map:
wrong turns:
shortcut used:
combat/gathering excluded or included:
landmarks remembered afterward:
subjective state: local / journey / remote:
notes:
```

Use ordinary ground walking for timed legs. If the test is interrupted, keep the observation and mark the run interrupted; do not estimate the missing time as if it were observed.

## Evidence boundary

- This runbook contains no physical results.
- A built map, passing unit test, or successful CI run does not prove the topology feels good when walked.
- Return the exact route observations to the Anchor. The Anchor decides whether a route passes, needs new geometry, or reveals a design question.
- P3 G1-G18, including the controlled G9 stationary range test, is a separate gate and remains open until its own evidence is recorded.
