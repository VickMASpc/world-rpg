# P3 cast movement policy

Status: ACCEPTED

Movement interruption is a capability of the cast mechanism.

It is **not** a universal law of every ability.

## Ability policy

Each ability carries one of:

- `INTERRUPT` — physical source movement breaks an active timed/channel cast.
- `ALLOW` — source movement does not itself break the cast.

Existing P3 fixture behavior defaults to `INTERRUPT`.

## Integration responsibility

The Minecraft server runtime observes physical source displacement while a cast
is active.

It only emits a MOVEMENT interruption when the active ability requests
`INTERRUPT`.

The integration layer does not decide that all casts must be stationary.

## Why this matters

The original combat doctrine values committed stationary casts.

That does not justify hard-coding every future ability, class, item proc, or
channel into the same rule.

Preserving explicit per-ability movement policy lets us build:

- heavy stationary spells,
- mobile utility casts,
- channels that permit repositioning,
- future effects that modify movement casting rules,

without replacing the cast engine.

## Deliberately unresolved

P3 still does not define:

- movement-speed penalties while casting,
- cast-while-moving temporary buffs,
- jump-specific rules,
- knockback exceptions,
- forced-movement exceptions,
- movement tolerance as a balance parameter.

Those require later gameplay decisions.
