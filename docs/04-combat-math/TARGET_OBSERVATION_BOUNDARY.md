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
- later: movement and facing facts.

It does not decide whether those facts are acceptable.

## RPG rules decide meaning

Ability conditions may decide:

- self target allowed or forbidden,
- source/target must be alive,
- same-world requirement,
- line-of-sight requirement,
- maximum range,
- later: facing arc, movement rule, faction/relation rule.

## Fresh observation

The CastController receives an AbilityObservationProvider.

It requests a fresh observation:

- when activation is attempted,
- when a timed cast resolves,
- at every scheduled channel tick.

Therefore a target moving out of range, dying, changing world, or breaking LOS can invalidate a cast that was legal when it started.

The integration does not snapshot legality forever at cast start.

## Missing observation

Missing/unavailable observation fails an ability rule that requires observed facts.

The kernel does not guess that an unavailable target is valid.

## Deliberately not decided

P3 does not yet decide:

- canonical ranges for actual abilities,
- PvP/faction hostility,
- facing arcs,
- movement tolerance,
- projectile travel,
- hit/miss,
- damage resolution.

Those remain data/P4/later concerns.
