# P4 simultaneous two-mob accidental-pull calibration

Status: REPRESENTATIVE DANGER SCENARIO

This is the first P4 scenario where ordinary enemies overlap their pressure.

It intentionally reuses the exact same level-20 routine enemy used by the solo
fixture. The enemies are not secretly weakened because there are two of them.

## Structure

The reference caster engages two routine enemies simultaneously.

The player focuses one enemy, then switches to the second after the first dies.

Both enemies:

- begin acting immediately,
- use the same 3-second routine strike,
- use the same attack power, miss and crit rules,
- continue overlapping pressure while alive.

The player uses the same 2.5-second routine bolt and the same mana pool as the
solo scenario.

No recovery occurs.

## Why player death is not an exception here

A bad pull is allowed to kill the player on some seeds.

That is a gameplay outcome, not a simulator failure.

Per-actor defeat metrics therefore track whether actor 1 (the player) actually
died in each run.

## Acceptance neighborhood

Across 256 deterministic seeds the current scenario requires:

- average elapsed time roughly 50-70 seconds,
- average mana spend roughly 85-110,
- player death rate low but non-zero: 1-15%,
- average enemy defeats at least 1.85 out of 2,
- zero explicit health or mana recovery.

A representative fixed seed also has to survive while ending below 25% of
starting health.

## Design meaning

This is the intended first shape of an accidental double pull:

- substantially more dangerous than two sequential routine fights,
- not an automatic character deletion,
- heavy enough that health/mana recovery afterwards is attractive,
- still solvable without adding defensive cooldowns or crowd control to the
  calibration character.

Later class kits should improve how a skilled player handles this situation
through control, interrupts, defensive tools and target prioritization.

Those systems should not be required merely to make the base simulator survive
at all.

## Still not tested here

- simultaneous caster enemies,
- interrupts,
- crowd control,
- knockback,
- dodge/parry/block,
- elite durability,
- dungeon pack coordination,
- threat/tanking.
