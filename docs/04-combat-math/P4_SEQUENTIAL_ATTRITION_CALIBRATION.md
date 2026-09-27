# P4 sequential routine attrition calibration

Status: SECOND REPRESENTATIVE SCENARIO

This scenario exists to test one specific promise from the game doctrine:

**routine combat pressure persists across encounters until the player explicitly
recovers.**

It is deliberately not the simultaneous two-mob accidental-pull scenario yet.

## Structure

The same level-20 calibration player fights two freshly created routine enemies
back-to-back.

Across both fights the following objects persist:

- player CombatActor,
- health,
- mana,
- stat state,
- player CastController,
- simulator clock,
- event journal,
- deterministic RNG stream.

No recovery method is called.

No resource pool is reset.

No new player actor is created.

## Acceptance neighborhood

Across 256 deterministic seeds:

- exactly two enemy defeat transitions must be observed,
- explicit mana recovery must remain zero,
- explicit health recovery must remain zero,
- average combined mana spend must remain roughly 80-120,
- average combined fight time must remain roughly 45-70 seconds.

A second direct regression also inspects the persistent session and requires:

- health after fight two < health after fight one < starting health,
- mana after fight two < mana after fight one < starting mana.

## Why this precedes the accidental-pull scenario

A simultaneous two-enemy pull mixes several questions:

- persistent resources,
- incoming-pressure multiplication,
- target priority,
- defensive tools,
- possibly interrupts and control.

This scenario isolates persistence first.

Once it is stable, the two-mob pull can measure the danger of a bad pull rather
than accidentally measuring whether the simulator reset the player.
