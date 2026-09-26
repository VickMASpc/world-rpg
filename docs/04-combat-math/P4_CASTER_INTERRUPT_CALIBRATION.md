# P4 caster heal / interrupt decision calibration

Status: REPRESENTATIVE ATTENTION SCENARIO

This scenario tests the combat doctrine that routine play can remain calm while
specific enemy casts demand attention.

## Setup

The level-20 reference caster fights one ordinary caster enemy.

The enemy otherwise uses routine 3-second physical strikes.

Once, after falling to roughly 70% health, it attempts a 4-second self-heal for
60 health.

The player policy is the only experimental variable.

Two batches use the same 256 seeds:

- INTERRUPT: react after 20 ticks (about one second) and cancel the heal through
  the production CastController interrupt path.
- IGNORE: allow the cast to resolve.

## Production mechanics exercised

The scenario uses:

- P3 timed casts,
- P3 CastInterruptedEvent with reason INTERRUPT,
- P4 healing resolution,
- P4 damage resolution,
- production target-alive/source-alive revalidation,
- simulator interruption metrics,
- resource-spend metrics,
- seeded batch comparison.

The interrupt itself is currently a direct headless decision against the
production CastController.

It is not yet an authored interrupt ability effect. That later integration
requires an explicit cast-control gateway/registry rather than a global
CastController lookup.

## Acceptance relationship

The scenario intentionally tests relative consequences rather than inventing a
new absolute combat envelope.

When the heal is interrupted:

- average fight time should remain in the existing 20-35 second routine band,
- healing applied should be zero,
- exactly one INTERRUPT should be recorded per run.

When the heal is ignored:

- the heal should restore roughly its authored amount,
- average fight time should increase by at least five seconds,
- average mana expenditure should increase by at least eight,
- the encounter should still remain a routine survivable fight.

## Design meaning

The enemy heal is not lethal if ignored.

Ignoring it costs **time and resources**.

That makes the cast worth noticing without turning every routine enemy into a
reflex test.

Later interrupt scenarios can layer:

- dangerous damage casts,
- school lockouts,
- interrupt cooldowns,
- fake/bait casts,
- multiple casters,
- dungeon coordination.
