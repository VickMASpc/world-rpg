# P3 Fabric runtime proof

Status: ACTIVE

P3 now exposes a server-side developer command:

```text
/worldrpg p3 proof
```

The command is restricted to permission level 2 and runs the deterministic P3 mechanism proof inside the logical Minecraft server.

It reports:

- remaining player mana,
- resulting player power,
- hostile target health,
- active aura count,
- whether the cast controller returned to idle.

Expected fixture result:

```text
P3 proof PASS | mana=40.0 power=130.0 targetHealth=70.0 auras=1 castIdle=true
```

These numbers are mechanism fixtures, not balance decisions.

## What this proves

- Fabric runtime can invoke the same Minecraft-independent RPG kernel used by CI.
- The kernel does not require a test-only environment.
- Command execution occurs on the logical server side.
- Stats, item modifier, aura modifier, persistent resource spending, timed cast, and channel composition survive the runtime boundary.

## What this does not prove

This is deliberately **not** the final P3 in-game combat proof.

It does not yet map:

- the actual player entity to CombatActor,
- a real hostile Minecraft entity to CombatActor,
- range,
- facing,
- line of sight,
- movement interruption,
- entity death,
- actual damage/healing,
- networking from action bar input.

The next Fabric integration step must use real world entities rather than expanding this synthetic fixture into a fake game.

The command exists as a truthful bridge, not as a substitute for the developer combat room.
