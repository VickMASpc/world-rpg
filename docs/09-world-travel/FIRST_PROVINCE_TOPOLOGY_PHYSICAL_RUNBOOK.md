# First Province Topology — Physical Measurement Runbook

Status: EXECUTABLE PHYSICAL PROOF — G9 REMAINS OPEN UNTIL RECORDED

This runbook is deliberately about **distance, route choice, landmark memory, quiet travel and learned geography**.

It is not a content demo.

Do not add enemies, quests, vendors, rewards or decorative architecture merely to make the run feel busier.

---

# 1. Test world

Use a fresh dedicated Superflat Overworld.

Recommended:

- cheats enabled;
- peaceful difficulty is acceptable;
- no movement-speed effects;
- no Elytra;
- no horse;
- no creative flight during measured runs;
- no teleport during measured runs;
- ordinary walking as the baseline;
- sprint only when a specific run intentionally tests sprint behavior.

The builder intentionally replaces route, river and landmark surface blocks across a very large area.

Do not run it in a valued world.

---

# 2. Build once

Stand where HOME A should be and run:

```text
/worldrpg province graybox build confirm
```

Expected:

- persistent graybox origin is stored;
- kilometer-scale route graph is built;
- river/bridge/fork exists;
- color-coded routes exist;
- node landmark pillars exist.

Route colors:

- light gray — safe/main road;
- red — dangerous shortcut;
- brown — learned return shortcut;
- lime — detour;
- cyan — outward/deeper route.

Node marker colors:

- red — A / HOME;
- yellow — B / WILD;
- orange — C / CORRIDOR;
- white — D / FORK;
- black — E / DANGER;
- light blue — F / REFUGE;
- purple — G / REGIONAL SETTLEMENT;
- cyan — H / BEYOND;
- lime — B2 / WORKLAND;
- green — R1 / LOCAL RUIN;
- magenta — R2 / UNFINISHED SITE.

The layout is a disposable spatial diagram, not final terrain or lore.

## Optional readable-place layer

If the colored-node version is too abstract to judge as a human world, build the authored readability layer on top of the same persisted topology:

```text
/worldrpg province graybox places build confirm
```

This does **not** replace the kilometer graph or require a new world.

It adds deliberately provisional but walkable place footprints at the existing nodes:

- Home A — internal cross street, ring path, several open building shells, larger inn/support hall, well, road gate;
- Refuge F — fenced compound, through-road, shelter, workshop, watchtower, camp;
- Regional Settlement G — walled footprint, broad north-south avenue, cross streets, plaza, civic/inn hall, multiple building shells and towers;
- Fork D — paved decision space and tower/arch landmark;
- Danger E — large dark ruined compound;
- Local Ruin R1 — smaller mossy ruin;
- Workland B2 — fenced work/farm footprint;
- Beyond H — outward gate/tower.

The builder clears above-ground blocks inside the three main authored settlement footprints before placing them. This is specifically meant to stop random Superflat village buildings from occupying the exact same test-space as A/F/G.

These structures are **navigation graybox**, not proposed final architecture.

The question becomes easier and more concrete:

- Can you walk through the settlement without feeling cramped?
- Can you identify the main exit without coordinates?
- Does G actually read as a larger settlement than A?
- Does F feel like a tiny last-safe-stop rather than another town?
- Is D a place you remember making a route decision?
- Does E look like somewhere distinct enough to remember and avoid?

---

# 3. Inspect the topology before measuring

```text
/worldrpg province graybox status
/worldrpg province graybox journeys
```

Use:

```text
/worldrpg province graybox nodes
```

only for setup/recovery.

Do **not** repeatedly use node coordinates while evaluating navigation memory.

The current journey definitions include:

- `a-to-r1-local`;
- `a-to-f-safe`;
- `a-to-g-safe`;
- `g-to-a-learned`;
- `f-to-h-outward`;
- `d-to-e-danger`.

---

# 4. Measurement commands

Start only while physically near the authored start node:

```text
/worldrpg province graybox measure start <journey> <first|known>
```

Finish only while physically near the authored end node:

```text
/worldrpg province graybox measure finish
```

Cancel a bad/aborted run:

```text
/worldrpg province graybox measure cancel
```

Review persisted latest measurements:

```text
/worldrpg province graybox measure history
```

A completed measurement records:

- elapsed server ticks/minutes;
- authored path length;
- actual horizontal distance traveled during the run;
- actual/authored route ratio;
- suspicious movement jumps;
- run count.

A teleport-sized movement discontinuity is not counted as ordinary walked distance and is reported as a suspicious jump.

---

# 5. Required physical runs

## Run A — local objective

At HOME A:

```text
/worldrpg province graybox measure start a-to-r1-local first
```

Travel via B and the detour to R1.

Then:

```text
/worldrpg province graybox measure finish
```

Prototype target: roughly 6–12 minutes.

Questions:

- Did it feel local rather than remote?
- Could you identify the point where you left the familiar road?
- Did returning toward A feel obvious without coordinates?

---

## Run B — first regional journey

At HOME A:

```text
/worldrpg province graybox measure start a-to-g-safe first
```

Follow the safe/light-gray route through the corridor, river/fork D and onward to G.

Finish at REGIONAL SETTLEMENT G.

Prototype target: roughly 25–40 minutes.

Questions:

- Did A and G feel like genuinely separate places?
- Did D become a memorable route decision?
- Were there several minutes where nothing demanded action?
- Did the destination feel anticipated before arrival?
- Did the route feel long because of geography, or merely stretched?

This measurement becomes the baseline for the learned return comparison.

---

## Run C — learned return

Immediately at G:

```text
/worldrpg province graybox measure start g-to-a-learned known
```

Take the brown learned shortcut toward B, then return to A.

The runtime compares this with the previously recorded first A -> G measurement.

There is intentionally no fixed minute band.

Success requires:

- materially shorter than the first regional journey;
- still long enough that geography has not collapsed;
- a real feeling of "I know a better way."

---

## Run D — safe journey to refuge

At HOME A:

```text
/worldrpg province graybox measure start a-to-f-safe first
```

Follow the safe route through D to F.

Prototype target: roughly 20–30 minutes.

Questions:

- Does F feel remote enough that reaching support matters?
- Is A psychologically far enough away by the time F is reached?
- Does the route contain a recognizable transition from local to remote?

---

## Run E — deeper outward direction

At F:

```text
/worldrpg province graybox measure start f-to-h-outward first
```

Follow the cyan outward route to H.

Prototype target: roughly 15–30 minutes before deeper content.

H is not a finished destination.

Its job is to prove that the province/world continues beyond the opening chapter.

---

## Run F — visible danger approach

At FORK D:

```text
/worldrpg province graybox measure start d-to-e-danger first
```

Approach E.

Finish the measurement at the danger marker, then physically retreat toward D.

No fixed time target is used.

Questions:

- Is E easy to remember from D?
- Does approaching it feel like a deliberate deviation?
- Can the player retreat and later describe where E sits relative to the safe route?

Later content will provide the actual high-level threat. This run validates geography first.

---

# 6. Manual mixed expedition

After the route graph itself is understood, perform one 60–180 minute mixed run.

Do **not** add filler solely to make it last an hour.

A valid mixed run may include:

- A -> known road;
- optional B2/R1 detour;
- D decision;
- F refuge;
- quiet travel;
- E/R2 cautious deviation;
- return or continued outward travel;
- existing Ashwood combat only where it makes geographic sense.

Record manually:

```text
run id:
start/end:
elapsed:
measured route legs:
wrong turns:
map/coordinate checks:
shortcut used:
quiet minutes that felt valid:
quiet minutes that felt empty:
landmarks remembered:
where "local" became "journey":
where "journey" became "remote":
moment you wanted to return:
notes:
```

---

# 7. Mental-map check

After several runs, stop using `nodes`.

Without coordinates, describe or sketch:

- A home;
- C main road;
- D river/fork;
- E danger;
- F refuge;
- G regional settlement;
- at least one shortcut;
- H deeper direction;
- one detour/ruin;
- which route is safest;
- which route is shorter/riskier.

Spatial precision is not the test.

Relationship memory is.

---

# 8. Failure interpretation

Do not automatically solve a bad run by adding mobs or shortening the road.

If a route fails, classify why:

- **too short** — destinations collapse together;
- **too long** — distance produces no additional meaning;
- **too straight** — no navigation relationship forms;
- **too illegible** — landmarks/forks cannot orient the player;
- **too repetitive** — distance exists but has no rhythm;
- **fake choice** — one route dominates without meaningful tradeoff;
- **refuge irrelevant** — support never feels far away;
- **shortcut trivial** — knowledge does not change travel;
- **shortcut destructive** — knowledge deletes the geography;
- **quiet but good** — leave it quiet;
- **quiet and empty** — fix geography/anticipation before adding encounter spam.

---

# 9. G9 closure bar

G9 does not close because the command builds successfully.

It closes only after physical evidence shows that:

- local destinations feel local;
- A and G feel genuinely separated;
- first A -> G is memorable;
- learned G -> A is meaningfully improved through knowledge;
- D creates a remembered route decision;
- F extends expedition reach;
- E is remembered as a dangerous deviation;
- quiet road time is legible;
- the player can reconstruct the graph afterward;
- a mixed 1–3 hour expedition can use the topology without requiring constant stimulation.

Only then should expensive first-province terrain production use this topology evidence.
