# P4 reference magnitude curves

Status: CALIBRATION DRAFT — NOT CLASS OR ITEM BUDGET CANON

World RPG needs level-scaled reference actors for simulation before class,
gear, talent and enemy budgets are finalized.

The first draft intentionally avoids exponential stat inflation.

## Curve form

For level L:

```text
x = L - 1

value(L) =
    base
    + linear * x
    + quadratic * x^2
```

The form is transparent, easy to inspect, and does not compound percentage
growth every level.

## Reference health

```text
100 + 6.5x + 0.025x^2
```

Approximate values:

| Level | Health |
| ---: | ---: |
| 1 | 100.0 |
| 10 | 160.5 |
| 20 | 232.5 |
| 50 | 478.5 |
| 75 | 717.9 |
| 100 | 988.5 |

The full journey therefore grows reference health by about 9.9x.

That is enough numerical space for progression while keeping older numbers
legible.

## Reference primary resource

```text
100 + 3x + 0.01x^2
```

Approximate values:

| Level | Resource |
| ---: | ---: |
| 1 | 100.0 |
| 10 | 127.8 |
| 20 | 160.6 |
| 50 | 271.0 |
| 75 | 376.8 |
| 100 | 495.0 |

This is a reference pool, not a declaration that every class uses mana or the
same resource scaling.

Some resources may remain fixed/normalized. Others may use different curves.

## Reference non-gear base power

```text
10 + 0.45x + 0.0015x^2
```

Approximate values:

| Level | Power |
| ---: | ---: |
| 1 | 10.0 |
| 10 | 14.2 |
| 20 | 19.1 |
| 50 | 35.7 |
| 75 | 51.5 |
| 100 | 69.3 |

This is deliberately smaller growth than health.

Production attack/spell/healing output will also include:

- authored ability bases/ranks,
- weapons,
- equipment,
- talents,
- buffs/debuffs,
- class-specific conversions.

The curve is therefore not a complete same-level TTK model.

## Why restraint matters

If baseline numbers grow by hundreds or thousands of times across 100 levels:

- equipment becomes disposable too quickly,
- nearby level bands stop interacting naturally,
- old locations/enemies become numerically meaningless,
- balancing authored long-lived regions becomes harder,
- percentages dominate intuition because raw values lose memory.

The draft instead reserves power for several overlapping systems without
requiring each system to inflate aggressively.

## Still unresolved

- class health multipliers,
- armor budgets,
- resistance budgets,
- gear stat budgets,
- weapon damage,
- ability rank/base-magnitude growth,
- primary-stat conversions,
- secondary-rating conversions,
- enemy-vs-player budget differences,
- elite/boss multipliers,
- same-level TTK targets.

Those are simulator/calibration work, not implicit consequences of these three
reference curves.
