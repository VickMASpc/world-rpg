# P3 physical Minecraft evidence runbook

Status: READY FOR MANUAL EXECUTION ONCE CURRENT HEAD CI IS GREEN

This runbook is the human-executable companion to `P3_GATE_VERIFICATION.md`.

It is intentionally explicit. If a row fails, record the failure; do not reinterpret the row until it passes.

All proof numbers, timings and commands are disposable developer fixtures, not production combat design.

---

# Test environment

Required:

- Minecraft 1.21.1 project build from the current `p4/math-simulator` head;
- Fabric development/runtime environment for this mod;
- a world where the tester has permission level 2 / cheats available for `/worldrpg` commands;
- an open flat-ish area;
- one opaque solid block in the hotbar for LOS testing.

Developer keys:

- F6 — Focus, self-targeted, real C2S path;
- F7 — Bolt, living entity under crosshair, real C2S path;
- F8 — Channel, living entity under crosshair, real C2S path.

Developer commands:

```text
/worldrpg p3 room spawn [distance]
/worldrpg p3 room move <distance>
/worldrpg p3 room health <0-100>
/worldrpg p3 room cast focus
/worldrpg p3 room cast bolt
/worldrpg p3 room cast channel
/worldrpg p3 room status
/worldrpg p3 room reset
/worldrpg p3 probe <target>
```

`room cast` is a **server-direct condition probe**. It bypasses C2S networking and says so in its feedback. Never use it as evidence for the network row.

---

# Fixture facts for this run

These values exist only to make manual behavior observable.

## Fresh actor state

Player:

```text
proof health = 100
proof mana = 100
base proof power = 100
proof staff = +10 power
```

Target:

```text
proof health = 100
proof mana = 100
proof power = 100
```

## Focus

```text
cost = 10 mana
instant
proof cooldown = 100 ticks (5 s)
global cooldown = 20 ticks (1 s)
applies Focus aura = +20 proof power
```

Therefore a fresh player should show:

```text
power 110 before Focus
power 130 after Focus
```

## Bolt

```text
cost = 20 mana
cast duration = 160 ticks (8 s)
proof cooldown = 220 ticks (11 s)
global cooldown = 20 ticks (1 s)
movement interrupts
max range = 12 blocks
requires LOS
on successful completion drains 15 target proof health
```

The long cast is intentional developer instrumentation.

## Channel

```text
cost = 30 mana
channel duration = 60 ticks (3 s)
interval = 20 ticks (1 s)
movement interrupts
max range = 10 blocks
requires LOS
three successful ticks of 5 proof-health drain each
```

Fresh target sequence:

```text
100 -> 95 -> 90 -> 85
```

---

# Evidence recording rule

For each row record:

```text
P3-G#:
head SHA:
procedure actually performed:
observed chat/status:
PASS / FAIL:
notes / screenshot reference:
```

A screenshot is useful but not mandatory if exact chat/status output is copied into the evidence record.

If a row fails:

1. keep the observed output;
2. classify kernel / integration / networking / fixture / documentation;
3. fix the owner;
4. repeat the failed row;
5. rerun affected neighboring rows when the fix touches casts, resources, targeting or networking.

---

# P3-G1 — entity-backed state

Fresh setup:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
/worldrpg p3 room status
```

Required:

- named real Husk exists in the world;
- status contains separate player and target actor IDs;
- both have RPG resources;
- player begins at mana 100;
- player power is 110 from base + proof item;
- target proof health is 100.

Record the full status line.

---

# P3-G2 — real client intent / network path

Keep the fresh target at 3 blocks.

1. Look directly at the Husk.
2. Press F7.

Required client feedback:

```text
P3 activation #...: accepted (cast started)
```

This row specifically proves:

```text
F7
-> target UUID selected client-side
-> C2S payload
-> server sequence guard
-> server activation
-> S2C structured response
```

`room cast bolt` is forbidden for this row because it bypasses networking.

---

# P3-G3 — persistent resource cost

Immediately after the accepted F7 activation, before the eight-second cast completes:

```text
/worldrpg p3 room status
```

Required:

```text
player mana = 80
player casting = true
target proof health = 100
```

After the cast completes, status again.

Required:

```text
player mana remains 80
target proof health = 85
```

There is no encounter reset or automatic mana refund.

---

# P3-G4 — committed timed cast

This can use the same fresh run as G2/G3.

Required chronology:

1. F7 returns `accepted (cast started)`;
2. mana is spent at activation;
3. target proof health remains 100 while the cast is in progress;
4. roughly eight seconds later the player receives:

```text
P3 cast completed: world_rpg:ability/proof/bolt
```

5. target proof health then becomes 85.

If target health changes at button press, the row fails.

---

# P3-G5 — movement interruption

Reset first:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
```

1. Look at target and press F7.
2. After acceptance, move the player before completion.

Required:

```text
P3 cast interrupted: MOVEMENT
```

Then:

```text
/worldrpg p3 room status
```

Required:

- target proof health still 100;
- player mana is 80 because activation cost remains committed;
- casting is false.

No implicit interruption refund is expected.

---

# P3-G6 — fresh range revalidation

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
```

1. Keep player completely still.
2. Look at target and press F7.
3. While the eight-second cast is active, run:

```text
/worldrpg p3 room move 20
```

The command must report that the same target UUID moved **without resetting RPG state**.

Required at cast resolution:

```text
P3 cast interrupted: TARGET_INVALID
```

Then status must show target proof health 100.

This proves fresh world observation at resolution, not source movement interruption.

---

# P3-G7 — LOS rejection at activation

Why this row uses direct room activation:

A solid wall prevents the temporary vanilla crosshair from selecting the entity, so F7 cannot construct the request. G2 already independently proves the network path.

Reset and spawn with enough placement room:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 5
```

Place an opaque solid block/wall directly between player and target so server LOS is blocked.

Run:

```text
/worldrpg p3 room cast bolt
```

Required:

- command feedback explicitly says `(network path bypassed)`;
- activation is rejected with the LOS condition failure;
- status shows player mana still 100;
- target proof health still 100;
- no cast is active.

Remove the wall after recording.

---

# P3-G8 — LOS revalidation after cast start

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 5
```

Ensure clear LOS.

1. Look at target and press F7.
2. Confirm accepted cast start.
3. Keep the player still.
4. During the eight-second cast, place an opaque block/wall between source and target.

Required at resolution:

```text
P3 cast interrupted: TARGET_INVALID
```

Status must show target proof health 100.

Remove the wall after recording.

---

# P3-G9 — explicit out-of-range rejection

The temporary vanilla crosshair cannot select a target twenty blocks away. Use the direct room condition probe.

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 20
/worldrpg p3 room cast bolt
```

Required:

- feedback says `(network path bypassed)`;
- activation rejected by max-range condition;
- player mana remains 100;
- target proof health remains 100;
- no cast starts.

---

# P3-G10 — deterministic channel schedule

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
```

Look at target and press F8. Do not move.

Required:

- activation accepted / channel cast starts;
- player mana becomes 70;
- three developer `P3 resource tick` messages occur at scheduled intervals;
- target proof-health transitions are visible as:

```text
100 -> 95
95 -> 90
90 -> 85
```

- then the channel completes;
- status shows target proof health 85 and casting false.

If only one combined `100 -> 85` mutation occurs, the row fails.

---

# P3-G11 — channel interruption

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
```

1. Press F8.
2. Let the first `100 -> 95` resource tick appear.
3. Move the player immediately afterward.

Required:

```text
P3 cast interrupted: MOVEMENT
```

No second/third channel resource tick may occur after interruption.

Status should leave target proof health at 95 if movement occurred after exactly the first tick.

The exact health may be lower if the tester moved only after another scheduled tick already committed; the evidence must therefore preserve chat order. The invariant is that **no future scheduled tick occurs after the MOVEMENT interruption event**.

---

# P3-G12 — aura/item shared stat engine

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
/worldrpg p3 room status
```

Required baseline player power:

```text
110
```

Press F6.

Required activation response: accepted instant.

Then:

```text
/worldrpg p3 room status
```

Required:

```text
player mana = 90
player power = 130
player auras = 1
```

The same StatSheet therefore contains:

```text
100 base
+10 proof item
+20 Focus aura
=130
```

---

# P3-G13 — RPG exhaustion independent of vanilla entity life

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
/worldrpg p3 room health 0
/worldrpg p3 room status
```

Required before activation:

- target Husk is visibly still present/alive in Minecraft;
- target RPG proof health is 0.

Look at target and press F7.

Required:

- server rejects the activation because target RPG resource-state condition fails;
- player mana remains 100;
- no cast starts;
- Husk remains physically present.

This proves RPG exhaustion is not accidentally delegated to vanilla Husk death.

---

# P3-G14 — server cooldown and GCD authority

Reset:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 3
```

## Global cooldown

1. Look at target so F7 is ready to submit.
2. Press F6.
3. Immediately press F7 within one second.

Required F7 response: rejected because global cooldown is still active.

## Ability cooldown

4. Wait until more than one second has passed from F6, but less than five seconds total.
5. Press F6 again.

Required: rejected because Focus's own ability cooldown remains active after the GCD has expired.

The exact remaining tick count is fixture timing, not balance doctrine.

---

# P3-G15 — replay / duplicate protection

Required evidence is already automated through `P3RequestSequenceTrackerTest`.

In-world packet replay is optional for P3 exit.

Do not invent a manual test unless a dedicated packet replay control is later added.

---

# P3-G16 — cleanup and state rebuild

Start from any active room.

Record current status/actor IDs, then:

```text
/worldrpg p3 room reset
/worldrpg p3 room status
```

Required:

- reset removes/discards the owned target;
- status reports no active developer target.

Then:

```text
/worldrpg p3 room spawn 3
/worldrpg p3 room status
```

Required:

- a new real target exists;
- fresh proof resources are restored by state reconstruction;
- no old aura/cast/resource damage leaks into the new room state.

Actor numeric IDs need not be relied upon as a player-facing identity guarantee; the requirement is clean rebuilding and distinct source/target binding.

---

# P3-G17 — physical distance / LOS / facing observation

With a close target, run for example:

```text
/worldrpg p3 probe @e[type=minecraft:husk,sort=nearest,limit=1]
```

Record the server probe summary.

Then deliberately change physical facts:

- turn away from target and probe again;
- move target with `/worldrpg p3 room move 10` and probe again;
- insert/remove a solid LOS blocker and probe again.

Required:

- distance changes consistently with relocation;
- facing observation changes when the player turns;
- LOS changes when the blocker is inserted/removed.

This row is observational; it proves that target conditions are fed by real Minecraft world facts rather than fixture constants.

---

# P3-G18 — headless parity / CI

At the exact tested head SHA, GitHub CI must remain green.

Required:

- compile/build succeeds;
- kernel/integration unit tests remain green;
- the manual harness patch has not broken the headless proof.

Record the run ID or link in the evidence file.

---

# Recommended row order

For the least contamination and least repeated setup:

```text
G1 -> G2 -> G3 -> G4
reset
G5
reset
G6
reset
G7
reset
G8
reset
G9
reset
G10
reset
G11
reset
G12
reset
G13
reset
G14
G15 automated
G16
G17
G18 CI
```

---

# Exit rule

Do not close P3 because the run "basically works."

P3 exits only when each required physical row has recorded evidence and every failure has either been fixed and rerun or remains explicitly blocking.

The proof claim is narrow but important:

> Minimal client intent can enter through Fabric networking, be resolved authoritatively against real Minecraft entities/world facts, execute deterministic generic RPG mechanics, preserve state across actions, and report structured outcomes without bespoke spell code.
