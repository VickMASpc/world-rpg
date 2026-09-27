# P3 physical Minecraft evidence — 2026-09-22

Status: PARTIAL MANUAL RUN RECORDED — GATE REMAINS OPEN

Test candidate requested before this run:

`763681e6775058e2cd6c3d8fb9e96333677e7d5f`

The branch remained on that exact SHA when this evidence was recorded. The tester's pasted client log did not itself print a Git SHA, so this file records that limitation rather than pretending the client log cryptographically identifies the build.

Exact candidate CI evidence is tracked separately by P3-G18.

## Evidence discipline

This file records only what the supplied log actually demonstrates.

Statuses:

- **PASS** — supplied physical output is sufficient for the row;
- **PARTIAL** — useful evidence exists, but one or more required observations/setup conditions were not captured;
- **PENDING** — supplied output does not exercise the row;
- **UNRESOLVED** — output is potentially interesting/suspicious but does not establish pass or failure.

The legacy `P3 proof PASS` helper line is not treated as a substitute for the physical matrix.

---

# Supplied observations

## Timed Bolt / real activation response

```text
[16:55:09] P3 activation #0: accepted — cast started
[16:55:17] P3 cast completed: world_rpg:ability/proof/bolt
[16:55:17] P3 resource tick: actor=2 resource=world_rpg:resource/proof_health 100.0 -> 85.0
```

Interpretation:

- structured numbered activation response was received;
- it does not contain the explicit `(network path bypassed)` marker used by `room cast`;
- Bolt remained committed for roughly eight seconds;
- target RPG health changed only at completion in the supplied chronology.

## Channel schedule

```text
[16:55:22] P3 activation #1: accepted — cast started
[16:55:23] P3 resource tick: actor=2 resource=world_rpg:resource/proof_health 85.0 -> 80.0
[16:55:24] P3 resource tick: actor=2 resource=world_rpg:resource/proof_health 80.0 -> 75.0
[16:55:25] P3 resource tick: actor=2 resource=world_rpg:resource/proof_health 75.0 -> 70.0
[16:55:25] P3 cast completed: world_rpg:ability/proof/channel
```

Interpretation:

- three separate scheduled resource mutations are observable;
- they occur about one second apart;
- completion occurs after the third tick;
- target was not fresh for this run, so the exact fresh-room `100 -> 95 -> 90 -> 85` sequence was not captured.

## Instant activation

```text
[16:55:29] P3 activation #2: accepted — instant
```

This is compatible with the real F6 Focus path, but the supplied excerpt does not include an immediate fresh status before/after this activation, so G12 is not closed by this line alone.

## Twenty-block spawn followed later by accepted actions

```text
[16:56:19] P3 developer target spawned 20 blocks ahead | UUID=1354dc46-700f-47cb-be6b-dac9614a418b | F6 Focus, F7 Bolt, F8 Channel
[16:56:31] P3 activation #3: accepted — instant
[16:56:32] P3 activation #4: accepted — cast started
[16:56:40] P3 cast completed: world_rpg:ability/proof/bolt
[16:56:40] P3 resource tick: actor=3 resource=world_rpg:resource/proof_health 100.0 -> 85.0
```

This is **UNRESOLVED**, not a recorded range failure.

The authored Bolt condition is max range 12 blocks, but the log records only the target's spawn distance at 16:56:19. It does not record the player's position at 16:56:32. Thirteen seconds elapsed between spawn and Bolt activation, which is enough for the player to have moved into legal range.

Close G9 using the deterministic procedure instead:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 20
/worldrpg p3 room cast bolt
/worldrpg p3 room status
```

Do not move the player between those commands.

## State-preserving developer target relocation

```text
[16:57:09] P3 developer target spawned 2 blocks ahead | UUID=2562dac3-aaa6-46c9-a7fc-e25b4a9fa2c6
[16:57:16] P3 developer target moved to 2 blocks ahead without resetting RPG state | UUID=2562dac3-aaa6-46c9-a7fc-e25b4a9fa2c6
[16:57:21] P3 developer target moved to 6 blocks ahead without resetting RPG state | UUID=2562dac3-aaa6-46c9-a7fc-e25b4a9fa2c6
```

This confirms the manual harness preserves target UUID while relocating it. It is useful setup evidence for G6/G17, but no active cast was shown during relocation, so it does not close G6.

## Direct server condition probe / room status

```text
[16:57:31] P3 direct room activation world_rpg:ability/proof/focus: accepted (instant) (network path bypassed)
[16:57:48] P3 room | player[actor=1 health=100.0 mana=0.0 power=130.0 auras=1 casting=false] target[actor=4 health=100.0 mana=100.0 power=100.0 auras=0 casting=false]
```

Useful facts:

- direct-room path labels itself as networking bypassed as designed;
- source and target have distinct RPG actor IDs;
- target owns RPG resources;
- player power 130 / one aura is visible.

Limitations:

- this is not a fresh G1 status snapshot;
- player mana is already exhausted from prior testing;
- no baseline power 110 was captured immediately before Focus;
- therefore G1 and G12 remain partial.

## Probe attempt

```text
[16:58:07] Unknown or incomplete command ...g p3 probe<--[HERE]
[16:58:13] P3 target probe | sourceActor=1 targetActor=1 sameEntity=true sameWorld=true sourceAlive=true targetAlive=true lineOfSight=true distance=0.0 facingDot=1.0
```

The successful probe targeted the player itself (`sourceActor=1 targetActor=1`).

It does **not** satisfy G17, which needs the Husk and deliberate changes to distance/facing/LOS.

Use:

```text
/worldrpg p3 probe @e[type=minecraft:husk,sort=nearest,limit=1]
```

and then repeat after turning away, moving the target, and inserting/removing a solid blocker.

## Legacy aggregate helper

```text
[16:58:29] P3 proof PASS | mana=40.0 power=130.0 targetHealth=70.0 auras=1 castIdle=true
```

This remains useful smoke evidence for the older aggregate proof scenario but is **not** accepted as a substitute for any missing physical G-row.

---

# Matrix assessment from this run

| ID | Status | Evidence / reason |
| --- | --- | --- |
| P3-G1 | PARTIAL | Distinct player/target actor IDs and RPG resources are visible, but no fresh reset/spawn/status baseline with mana 100, power 110, target health 100 was captured. |
| P3-G2 | **PASS** | Numbered client activation `#0` was accepted as a Bolt cast, then completed; direct-room activations use a different explicit `network path bypassed` message. |
| P3-G3 | PARTIAL | Bolt effect and later persistence are visible, but required status during/after that Bolt showing player mana 80 was not captured. |
| P3-G4 | **PASS** | Accepted at 16:55:09; completed at 16:55:17; target health changed `100 -> 85` at completion, not at button press in supplied chronology. |
| P3-G5 | PENDING | No MOVEMENT interruption evidence supplied. |
| P3-G6 | PARTIAL | Same UUID relocation without state reset works; no active Bolt + move-to-20 + TARGET_INVALID chronology supplied. |
| P3-G7 | PENDING | No blocked-LOS direct activation evidence supplied. |
| P3-G8 | PENDING | No LOS blocker inserted during active Bolt evidence supplied. |
| P3-G9 | UNRESOLVED | 20-block spawn was later followed by a successful Bolt, but player position at activation was not logged. Run deterministic direct probe while stationary. |
| P3-G10 | PARTIAL (strong) | Three distinct one-second channel ticks and completion are visible, but run began from target health 85 rather than a fresh 100. Rerun after reset to close exact row. |
| P3-G11 | PENDING | No channel MOVEMENT interruption chronology supplied. |
| P3-G12 | PARTIAL | Instant activation and later `power=130 auras=1` are visible, but fresh baseline power 110 and post-F6 mana 90/status were not captured together. |
| P3-G13 | PENDING | No RPG-health-zero rejection evidence supplied. |
| P3-G14 | PENDING | No explicit GCD then ability-cooldown rejection evidence supplied. |
| P3-G15 | AUTOMATED PASS | Existing request-sequence unit proof; physical replay optional. |
| P3-G16 | PENDING | No reset → no-target → respawn fresh-state chronology supplied. |
| P3-G17 | PENDING | Supplied probe targeted self, not Husk; no physical distance/facing/LOS transition recorded. |
| P3-G18 | **PASS** | Exact requested candidate `763681e6775058e2cd6c3d8fb9e96333677e7d5f` was green in CI run `35770679890` before physical handoff. |

## Gate state after first run

P3 remains open.

Clean physical passes newly recorded:

- P3-G2 — real client/network activation path;
- P3-G4 — committed timed-cast semantics;
- P3-G18 — exact candidate CI/headless parity.

Automated G15 was already satisfied.

Next manual priority should be the rows that require the least setup and close the strongest current partials:

1. G1/G3 from one fresh Bolt run with status before/during/after;
2. G10 fresh Channel;
3. G12 fresh Focus baseline/post-status;
4. G9 deterministic stationary 20-block direct condition probe;
5. G17 correct Husk probe;
6. then interruption/revalidation rows G5/G6/G7/G8/G11/G13/G14/G16.
