# P3 target observation boundary

Status: ACCEPTED

Target legality is split into two responsibilities.

## Integration observes facts

The Minecraft integration layer may observe:

- whether source and target still exist,
- whether they are the same entity,
- whether they are in the same world,
- alive state,
- line of sight,
- physical distance,
- source-facing alignment toward the target.

It does not decide whether those facts are acceptable.

## RPG rules decide meaning

Ability conditions may decide:

- self target allowed or forbidden,
- source/target must be alive,
- same-world requirement,
- line-of-sight requirement,
- maximum range,
- optional facing arc,
- future faction/relation rules.

No facing restriction is applied unless an ability asks for one.

## Facing representation

The integration reports a source-facing dot product:

- `1.0` — directly toward target,
- `0.0` — target is approximately perpendicular,
- `-1.0` — directly behind source.

The generic condition may convert a full arc in degrees into the appropriate
dot threshold.

Examples:

- 180° — front hemisphere,
- 90° — narrow forward cone,
- 360° — no meaningful orientation restriction.

## Fresh observation

The CastController receives an AbilityObservationProvider.

It requests a fresh observation:

- when activation is attempted,
- when a timed cast resolves,
- at every scheduled channel tick.

Therefore a target moving out of range, dying, changing world, breaking LOS,
or leaving a required facing arc can invalidate a cast that was legal when it
started.

The integration does not snapshot legality forever at cast start.

## Missing observation

Missing/unavailable observation fails an ability rule that requires observed
facts.

The kernel does not guess that an unavailable target is valid.

## Deliberately not decided

P3 does not yet decide:

- canonical ranges for actual abilities,
- PvP/faction hostility,
- which abilities require facing,
- projectile travel,
- hit/miss,
- damage resolution.

Those remain data/P4/later concerns.
