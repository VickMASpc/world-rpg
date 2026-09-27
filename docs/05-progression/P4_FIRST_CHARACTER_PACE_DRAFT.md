# P4 first-character pace draft

Status: CALIBRATION DRAFT — NOT FROZEN XP CURVE

The constitution fixes a 1-100 journey.

The progression bible currently targets roughly 350-500 hours for a blind
first character.

P4 now has a measurable midpoint calibration profile:

```text
level cap: 100
level 1 -> 2 target: 0.75 h
level 99 -> 100 target: 6.50 h
growth exponent: 0.60
total level-1-to-100 target: ~429.25 h
```

## Shape

For a transition from level L to L+1:

```text
p = (L - 1) / 98

hours(L) =
    firstHours
    + (finalHours - firstHours) * p^exponent
```

This produces a front-loaded increase that gradually flattens rather than a
late exponential wall.

Approximate cumulative targets:

| Reached level | Cumulative hours |
| ---: | ---: |
| 10 | 13.7 |
| 20 | 38.6 |
| 30 | 70.5 |
| 40 | 108.2 |
| 50 | 151.0 |
| 60 | 198.4 |
| 70 | 250.3 |
| 80 | 306.1 |
| 90 | 365.9 |
| 100 | 429.3 |

These are journey-time targets, not requirements that every player or route
match the table.

## Why time is modeled before XP

XP is an economy.

Its requirements are meaningless until we know the expected reward throughput
from:

- authored quests,
- enemy kills,
- exploration,
- dungeons,
- professions,
- hidden discoveries,
- other accepted progression sources.

If P4 chose XP-per-level first, it could make insufficient content look
mathematically complete by simply demanding more repetition.

That would violate:

**slow means lived-in, not padded.**

Therefore:

```text
desired lived-play pace
-> content/reward throughput measurements
-> XP requirements and reward values
```

not the reverse.

## Interpretation

The profile deliberately makes early progress readable.

The first ten levels still consume a substantial opening chapter rather than a
throwaway tutorial, but they do not require late-game hours per level.

By the middle of the journey, individual levels take several hours.

Late levels are long enough that a level can contain multiple expeditions,
returns to civilization, profession activity, dungeon work and travel rather
than one short quest hub.

## What this profile does NOT freeze

It does not yet define:

- XP required by level,
- XP/hour,
- rested XP,
- quest reward values,
- kill reward values,
- dungeon reward values,
- profession reward values,
- alt/second-character pacing,
- death-time cost,
- exact content hours per region,
- exact combat TTK.

The profile may move inside the accepted 350-500 hour band when vertical-slice
evidence arrives.

Changing the 1-100 invariant or the accepted overall band is a larger design
decision and should be documented explicitly.
