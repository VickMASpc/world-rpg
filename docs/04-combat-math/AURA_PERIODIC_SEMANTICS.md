# P3 periodic aura semantics

Status: ACCEPTED FOR P3 MECHANISM

Periodic aura timing is deterministic and independent of render frame rate.

## Rules

- A periodic aura stores its next scheduled gameplay tick.
- The interval is part of the aura definition mechanism.
- P3 does not apply haste or other final timing formulas.
- Reapplication explicitly chooses whether to keep the existing schedule or reset it.
- Delayed server advancement catches up missed scheduled ticks in chronological order.
- Catch-up is bounded per aura to prevent pathological time jumps from causing unbounded work.
- If a tick is scheduled exactly at the aura's expiry tick, that tick executes before expiry.
- Expiry follows all periodic work due at that same tick.
- An indefinite aura may tick indefinitely but remains subject to catch-up limits.
- Periodic gameplay executes through the same generic EffectSequence used by abilities.
- Periodic effects emit ordinary combat events; they do not bypass reaction topology.

## Deliberately not decided here

P4/later systems still decide:

- whether haste changes interval,
- whether haste changes aura duration,
- partial/final tick formulas,
- damage/healing coefficients,
- snapshotting vs dynamic stat reads,
- dispel categories,
- pandemic-style refresh windows,
- per-effect proc semantics.

The mechanism exists without choosing the balance doctrine early.
