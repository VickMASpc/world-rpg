# P3 gate verification matrix

Status: ACTIVE — FIRST MANUAL EVIDENCE RECORDED, GATE REMAINS OPEN

P3 does not close because the code compiles or because one happy-path cast works.

The gate closes only when the entity-backed Minecraft harness demonstrates the behaviors below and the results are recorded.

The executable step-by-step procedure is:

`docs/16-implementation/P3_PHYSICAL_EVIDENCE_RUNBOOK.md`

That runbook is authoritative when a short matrix description is ambiguous.

First recorded physical run:

`docs/16-implementation/P3_PHYSICAL_EVIDENCE_2026-09-22.md`

## Test environment

Current developer harness:

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

Developer inputs:

- F6 — self Focus over the real C2S path;
- F7 — targeted Bolt over the real C2S path;
- F8 — targeted Channel over the real C2S path.

`room cast` deliberately bypasses the C2S/replay layer and prints that fact in feedback. It exists only to construct server validation cases that the temporary vanilla crosshair cannot submit, such as a target hidden behind a wall or beyond vanilla crosshair reach. It is never evidence for the networking row.

All numbers/timings are fixture values. They are not balance targets.

## Required evidence

| ID | Behavior | Procedure | Required result | Evidence |
| --- | --- | --- | --- | --- |
| P3-G1 | Entity-backed state | Reset; spawn at 3; run room status | Separate player/target actor bindings and persistent RPG resources exist; player power includes proof item | **PARTIAL 2026-09-22** — distinct player/target actor IDs and RPG resources observed, but no fresh baseline status snapshot |
| P3-G2 | Client intent only | Press F7 while looking at close target | Real C2S request reaches server and S2C response reports accepted/cast-started | **PASS 2026-09-22** — numbered activation #0 accepted/cast-started, then completed; unlike direct room path, no network-bypassed marker |
| P3-G3 | Persistent resource cost | Inspect status during/after accepted Bolt | Player mana is spent at activation and remains spent; no encounter reset | **PARTIAL 2026-09-22** — accepted Bolt observed, but required mana=80 status during/after was not captured |
| P3-G4 | Committed timed cast | Press F7 and stand still | Target RPG health remains unchanged during cast and changes only on completion | **PASS 2026-09-22** — activation 16:55:09, completion/resource mutation 16:55:17, health 100→85 at completion |
| P3-G5 | Movement interruption | Start F7 then move source before completion | Server emits MOVEMENT; no Bolt effect applies; activation cost is not refunded | PENDING |
| P3-G6 | Fresh range revalidation | Start legal F7 at 3; keep source still; `room move 20` before completion | Same target state is preserved, fresh range fails at resolution, TARGET_INVALID emitted, no effect applies | **PARTIAL 2026-09-22** — room move preserved target UUID/state, but no active-cast revalidation chronology captured |
| P3-G7 | LOS at activation | Spawn close; put solid wall between actors; use `room cast bolt` | Direct condition probe is rejected by server LOS; no mana/effect mutation; feedback explicitly says network path bypassed | PENDING |
| P3-G8 | LOS revalidation | Start legal F7 with clear LOS; insert solid blocker before completion | Cast becomes TARGET_INVALID before effect application | PENDING |
| P3-G9 | Out-of-range rejection | Spawn at 20; use `room cast bolt` | Direct condition probe is rejected by max-range; no mana/effect mutation; network path explicitly bypassed | **UNRESOLVED 2026-09-22** — 20-block spawn was later followed by successful Bolt, but player position at activation was not logged; run deterministic stationary direct probe |
| P3-G10 | Channel schedule | Spawn close; press F8; do not move | Three distinct resource-change ticks are visible before completion: 100→95→90→85 | **PARTIAL (strong) 2026-09-22** — three distinct one-second ticks observed (85→80→75→70) and completion after third; rerun fresh room for exact baseline sequence |
| P3-G11 | Channel interruption | Start F8; after a visible tick move source | MOVEMENT emitted and no future scheduled channel tick occurs after interruption | PENDING |
| P3-G12 | Aura/item shared stats | Record baseline; press F6; inspect status | Shared stat engine shows 100 base +10 item +20 aura = 130 power | **PARTIAL 2026-09-22** — instant activation and later power=130/auras=1 observed, but fresh power=110 baseline and immediate post-F6 mana/status were not captured together |
| P3-G13 | RPG exhaustion | `room health 0`; request hostile ability via F7 | Server rejects by RPG target-resource condition while invulnerable Minecraft Husk remains physically present | PENDING |
| P3-G14 | Cooldown/GCD authority | F6 then immediate F7; later retry F6 after GCD but before Focus cooldown | Server independently rejects GCD, then ability cooldown | PENDING |
| P3-G15 | Replay/duplicate protection | Automated request-sequence test | Equal/older request sequence is rejected before gameplay mutation | **AUTOMATED PASS** — in-world packet replay optional |
| P3-G16 | Cleanup | Reset; verify target gone/status unavailable; respawn | Owned target is discarded and fresh actor/resources rebuild with no leaked cast/aura/damage state | PENDING |
| P3-G17 | Distance/LOS/facing observation | Use `/worldrpg p3 probe <target>` while turning, relocating target, adding/removing blocker | Server-observed distance/facing/LOS change consistently with physical world facts | **PENDING 2026-09-22** — supplied successful probe targeted self (`sourceActor=1 targetActor=1 distance=0`), so it is not valid Husk evidence |
| P3-G18 | Headless parity | Run CI at exact manually tested SHA | Pure-kernel, integration and build checks remain green | **PASS** — requested candidate `763681e6775058e2cd6c3d8fb9e96333677e7d5f`, CI run `35770679890` green before physical handoff |

## Why G6, G7 and G9 use special setup

### G6

Moving the source would trigger the ability's movement interruption first and would therefore not prove range revalidation. The target must be moved while preserving entity UUID/RPG state.

### G7

A solid wall blocks the vanilla crosshair before it can select the target. The server-direct room activation constructs the LOS-invalid request without pretending it passed through networking. G2 separately proves networking.

### G9

A target beyond the proof ability's range is also beyond the temporary vanilla crosshair's useful selection reach. The direct condition probe isolates the server range rule. G2 separately proves networking.

The first manual run contained a target that was *spawned* at 20 blocks and was successfully hit later, but the player's position at the later activation was not recorded. That chronology is not sufficient to call a range failure because the source could have moved into legal range during the intervening time. G9 therefore remains UNRESOLVED until the stationary direct probe is run.

## Failure discipline

A failed row is not fixed by weakening the expected result.

For each failure:

1. record actual behavior;
2. identify whether the bug is kernel, integration, networking, fixture policy, or documentation;
3. fix the owning layer;
4. repeat the failed row;
5. rerun affected neighboring rows if event ordering, resources, casts, targeting or networking changed.

## What this matrix deliberately does not validate

P3 does not claim to prove:

- final damage equations;
- final health/mana values;
- class rotations;
- enemy AI;
- faction rules;
- final action bars;
- final target selection UX;
- VFX quality;
- final combat pacing;
- PvP;
- world content.

Those belong to later gates.

## P3 exit statement

When every required physical row has recorded evidence, P3 may state:

> The World RPG kernel can accept minimal client intent, resolve it authoritatively against real Minecraft entities and world facts, execute deterministic generic RPG mechanics, preserve state across actions, and report structured outcomes without relying on bespoke spell code.

Anything weaker is not the P3 gate.
