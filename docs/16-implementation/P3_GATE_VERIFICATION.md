# P3 gate verification matrix

Status: ACTIVE — IMPLEMENTATION READY, MANUAL EVIDENCE NOT YET RECORDED

P3 does not close because the code compiles or because one happy-path cast works.

The gate closes only when the entity-backed Minecraft harness demonstrates the
kernel behaviors below and the results are recorded.

## Test environment

Use the current P3 developer harness:

```text
/worldrpg p3 room spawn [distance]
/worldrpg p3 room status
/worldrpg p3 room reset
```

Developer inputs:

- F6 — self Focus,
- F7 — targeted Bolt,
- F8 — targeted Channel.

All numbers are fixture values. They are not balance targets.

## Required evidence

| ID | Behavior | Procedure | Required result | Evidence |
| --- | --- | --- | --- | --- |
| P3-G1 | Entity-backed state | Spawn target at 3 blocks; run room status | Separate player/target actor IDs and persistent RPG resources exist | PENDING |
| P3-G2 | Client intent only | Press F7 while looking at target | C2S request reaches server and server returns accepted/cast-started | PENDING |
| P3-G3 | Persistent resource cost | Cast Bolt, inspect status, cast again | Player mana remains spent between actions; no encounter reset | PENDING |
| P3-G4 | Committed timed cast | Press F7 and stand still | Effect resolves only after cast duration, not at button press | PENDING |
| P3-G5 | Movement interruption | Start F7 then move before completion | Server emits MOVEMENT interruption; target RPG health is unchanged by the interrupted cast | PENDING |
| P3-G6 | Fresh range revalidation | Start legal cast, move far enough before completion | Cast becomes invalid at resolution; no effect applies | PENDING |
| P3-G7 | LOS at activation | Put a solid wall between source and target, press F7 | Structured LOS rejection; no resource/effect mutation beyond policy-defined activation behavior | PENDING |
| P3-G8 | LOS revalidation | Start cast with LOS, then break LOS before completion | Cast is interrupted/invalidated before effect application | PENDING |
| P3-G9 | Out-of-range rejection | Spawn target beyond Bolt fixture range and request Bolt | Structured out-of-range rejection | PENDING |
| P3-G10 | Channel schedule | Spawn at valid range; press F8; do not move | Three scheduled fixture ticks occur before cast completes | PENDING |
| P3-G11 | Channel interruption | Start F8 then move | Remaining channel ticks stop after MOVEMENT interruption | PENDING |
| P3-G12 | Aura/item shared stats | Press F6 then inspect room status | Player power shows base + item + aura through the shared stat engine | PENDING |
| P3-G13 | RPG exhaustion | Reduce target proof-health to zero, request hostile ability again | Request is rejected by RPG resource-state condition while Minecraft husk may remain physically alive | PENDING |
| P3-G14 | Cooldown/GCD authority | Repeat activation during fixture cooldown/GCD | Server rejects according to its own cooldown state | PENDING |
| P3-G15 | Replay/duplicate protection | Replay equal/older activation request sequence through a developer packet test | Server rejects before gameplay mutation | AUTOMATED UNIT PROOF; IN-WORLD PACKET REPLAY OPTIONAL |
| P3-G16 | Cleanup | Reset room; inspect world/session, then respawn | Owned target is discarded and actor state is rebuilt cleanly | PENDING |
| P3-G17 | Distance/LOS/facing observation | Use `/worldrpg p3 probe <target>` while repositioning/turning | Server-observed values change consistently with the physical world | PENDING |
| P3-G18 | Headless parity | Run CI | Pure-kernel proof and integration unit tests remain green | AUTOMATED |

## Failure discipline

A failed row is not fixed by weakening the expected result.

For each failure:

1. record actual behavior,
2. identify whether the bug is kernel, integration, networking, or fixture policy,
3. fix the owning layer,
4. repeat the failed row,
5. rerun the full matrix if event ordering, resources, casts, or targeting changed.

## What this matrix deliberately does not validate

P3 does not claim to prove:

- final damage equations,
- final health/mana values,
- class rotations,
- enemy AI,
- faction rules,
- final action bars,
- final target selection,
- VFX quality,
- final combat pacing,
- PvP,
- world content.

Those belong to later gates.

## P3 exit statement

When every required row has evidence, P3 may state:

> The World RPG kernel can accept minimal client intent, resolve it
> authoritatively against real Minecraft entities and world facts, execute
> deterministic generic RPG mechanics, preserve state across actions, and
> report structured outcomes without relying on bespoke spell code.

Anything weaker is not the P3 gate.
