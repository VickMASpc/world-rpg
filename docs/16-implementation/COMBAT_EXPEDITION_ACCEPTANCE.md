# Cycle 1 — First Real Combat Expedition — Physical Acceptance

## What this candidate must prove

Cycle 1 is not a renderer demo and it is not another isolated combat proof.

The candidate passes only when one physical Minecraft route proves the connected chain:

~~~text
authored location
    -> authored spawn group
    -> animated authored enemy
    -> server-authoritative World RPG combat
    -> player/target HUD
    -> authored enemy behavior
    -> authored defeat event
    -> authored loot
    -> ordered quest progression
    -> persistent RPG bag / quest state
~~~

P3 remains available as a regression harness, but the expedition below uses the production combat path.

## Build

~~~powershell
git switch p4/math-simulator
git pull --ff-only origin p4/math-simulator
.\gradlew.bat build
~~~

Use a disposable test save for fixture commands.

---

## A. Expand the physical route

If the current disposable save was built before the Ashwood route existed:

~~~text
/worldrpg slice reset confirm
/worldrpg slice build confirm
/worldrpg slice status
~~~

Slice reset does not erase player quest or RPG bag progression.

The rebuilt graybox should contain:

- Road Warden post near the origin;
- East Road checkpoint around +48 X;
- Refuge / Refuge Scout around +104 X;
- Collapsed Waystation around +160 X;
- a side path leaving the east road near +128 X and running south;
- **Ashwood Verge** around +128 X, +32 Z relative to the build origin.

The generic adventure runtime should now report **4 location bindings**:
checkpoint, refuge, waystation, and Ashwood Verge.

The +128/+32 geometry is acceptance-scale graybox only. It is not a statement about final First Province travel distance.

---

## B. Reach the third quest

### Fast path on the existing completed adventure test character

If both earlier quests are already complete, interact with Refuge Scout.

Expected next quest:

~~~text
Pressure in the Ashwood
~~~

Run:

~~~text
/rpgjournal
~~~

Expected current objective:

~~~text
find ashwood verge
~~~

### Full chain replay

To prove the chain from a clean RPG record:

~~~text
/worldrpg persistence resetplayer confirm
~~~

Then complete:

1. **Disappearances Along the East Road**
   - Warden accepts.
   - checkpoint completes visit.
   - first return click completes report only.
   - second return click turns in.
2. **Silence at the Waystation**
   - Scout accepts.
   - waystation completes visit.
   - first return click completes report only.
   - second return click turns in.
3. Interact with Scout again.
   - **Pressure in the Ashwood** must now accept.

Before quest #1 completion, Scout should expose the nearest locked quest in the chain (**Silence at the Waystation**), not the deeper Ashwood quest.

---

## C. Spawn ecology at Ashwood Verge

Walk the side path into Ashwood Verge.

Expected:

- the location-entry objective advances automatically;
- authored population appears while the player is near the hunting ground;
- target population settles around:
  - 3 **Ashwood Wolves**;
  - 1 **Ashwood Stalker**.

Run:

~~~text
/worldrpg combat status
~~~

With a full nearby population, the runtime should be approximately:

~~~text
authored mob runtime | bindings=4 spawnGrouped=4
~~~

Population can temporarily be lower after kills while respawn delay is active. It must not grow above the authored target merely because the server continues ticking.

Expected world behavior:

- Wolves acquire a nearby survival player inside their authored aggro range;
- nearby members of the same spawn group assist;
- authored runtime navigation pursues the target;
- pulling a mob beyond its authored leash causes it to disengage and return toward the hunting ground;
- creative/spectator players are not valid normal aggro targets.

---

## D. Creature presentation

The Ashwood family must render as the custom **World RPG Ashwood Wolf**, not the vanilla wolf model.

Runtime presentation is exported from the canonical editable source:

~~~text
assets-source/creatures/ashwood_wolf.bbmodel
~~~

The source contains 92 modeled pieces and authored animations. Runtime GeckoLib resources are generated from that source.

Verify:

- idle animation visibly moves the creature;
- walk animation plays during pursuit;
- first accepted engage/howl event triggers the howl animation;
- an accepted bite triggers the bite animation;
- the model uses the Ashwood texture and articulated silhouette rather than a vanilla wolf renderer.

A missing model, checkerboard texture, static T-pose-like creature, or vanilla wolf appearance fails this section.

---

## E. Production combat authority and HUD

The normal Cycle 1 combat input is:

~~~text
G = Field Strike
~~~

Look directly at an authored Wolf/Stalker.

Expected HUD:

- lower-left player RPG frame:
  - level;
  - World RPG health;
  - focus;
- upper-center target frame:
  - authored enemy name;
  - level;
  - World RPG health;
- bottom-center action slot:
  - [G] Field Strike;
- accepted/rejected ability feedback near the action slot.

Press **G**.

Expected:

- the server validates the target and ability;
- Field Strike consumes authored focus;
- damage comes from the P4 production combat resolver;
- target RPG health changes and the target frame reflects it;
- repeated use is constrained by authored cooldown/global-cooldown rules;
- focus regenerates through the production runtime.

Run:

~~~text
/worldrpg combat status
~~~

Expected player state begins around:

~~~text
hp=100.0/100.0 focus=100.0/100.0
~~~

and reflects combat changes.

### Authority failure checks

A normal vanilla attack must **not** bypass authored RPG health on an Ashwood enemy.

The authored mob's vanilla attack-damage attribute is zero. Enemy damage to the player must come through its authored bite ability / production resolver.

Any route where vanilla damage and RPG damage both apply for one authored attack fails this gate.

---

## F. Loot and quest defeat routing

Kill one regular **Ashwood Wolf** first.

Expected:

- a top-right RPG loot notice appears;
- /rpgbag reflects authored loot/copper;
- the Stalker quest objective does **not** complete.

Production Wolf loot may contain:

- Ashwood Fang;
- Ashwood Pelt;
- authored copper.

Production loot must not expose a world_rpg:dev/golden/... item ID.

Now defeat the **Ashwood Stalker**.

Expected:

- Stalker loot grants once;
- the quest objective **break the stalker** advances once;
- current quest objective becomes **report to scout**;
- killing ordinary Wolves still cannot satisfy the Stalker objective.

Repeated death handling must not duplicate loot or quest credit.

---

## G. Report and turn in

Return to Refuge Scout.

### First click

Expected:

- report objective completes;
- quest becomes READY_TO_TURN_IN;
- no quest reward yet.

### Second click

Expected:

- quest completes;
- reward grants exactly once:
  - Ashwood Pelt x2;
  - 45 copper.

Repeated Scout interactions must not duplicate the reward.

---

## H. Respawn and leash lifecycle

Stay near Ashwood Verge after defeating members of the population.

Expected:

- regular Wolf group begins restoring after its 200-tick authored respawn delay;
- Stalker begins restoring after its 400-tick authored respawn delay;
- group population returns toward target rather than multiplying without bound.

Leave the area far enough that no player is near the hunting-ground population window.

Expected:

- the runtime does not continuously manufacture distant mobs merely because the server is running.

---

## I. Save/reload evidence

After completing the expedition:

1. fully exit the world;
2. reopen the same save;
3. return to the route;
4. run:

~~~text
/rpgjournal
/rpgbag
/worldrpg slice status
/worldrpg combat status
~~~

Required persistence:

- quest completion history;
- RPG bag/copper;
- physical adventure location/NPC bindings;
- living authored-mob UUID -> mob/spawn-group bindings when those entities survive the reload.

Provisional for Cycle 1 and **not** a failure of this gate:

- player combat health/focus/cooldown state is currently session runtime state;
- in-memory respawn countdown resumes from a fresh runtime schedule after restart;
- target selection is a first-pass crosshair target frame, not a sticky MMO targeting system.

Those are explicit next-cycle work, not hidden persistence claims.

---

## Acceptance matrix

| Gate | Pass condition |
| --- | --- |
| A1 | Rebuilt slice includes physical Ashwood side route |
| A2 | Adventure runtime reports 4 location bindings |
| B1 | Scout quest chain progresses Warden -> Waystation -> Ashwood |
| B2 | Fresh player sees nearest locked prerequisite, not a deeper quest |
| C1 | Near Ashwood Verge, authored population tends to 3 Wolves + 1 Stalker |
| C2 | Population does not duplicate above authored target through ordinary ticking |
| C3 | Aggro, pack assist and leash return are physically visible |
| D1 | Custom Ashwood model/texture renders |
| D2 | Idle/walk animations render |
| D3 | Howl/bite action animations are driven by authored combat events |
| E1 | G/Field Strike uses server-authoritative production combat |
| E2 | HUD reflects player health/focus and authored target RPG health |
| E3 | Vanilla player attacks do not bypass authored enemy RPG health |
| E4 | Authored mob does not double-apply vanilla + RPG damage |
| F1 | Regular Wolf gives authored loot but no Stalker objective credit |
| F2 | Stalker death advances the defeat objective exactly once |
| F3 | Production loot contains no dev/golden item references |
| G1 | First Scout return reports only; second click turns in |
| G2 | Ashwood quest reward grants exactly once |
| H1 | Wolf/Stalker population restores after authored delay |
| I1 | Quest and RPG bag survive reload |
| I2 | Adventure bindings survive reload |
| I3 | Surviving authored-mob bindings survive reload |

Until these physical rows are run, Cycle 1 is an **implemented candidate**, not a physically accepted milestone.
