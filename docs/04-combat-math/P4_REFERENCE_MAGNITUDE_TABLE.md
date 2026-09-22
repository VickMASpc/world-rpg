# P4 reference magnitude table

Status: CALIBRATION SNAPSHOT

This table makes the current 1-100 reference curves inspectable without
requiring anyone to mentally evaluate the formulas.

These are reference-actor and simulator values, not final class templates or
gear budgets.

| Level | Health | Primary resource | Base power | Reference armor | Reference resistance | Rating for +1% |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 100.000 | 100.000 | 10.0000 | 33.333 | 17.647 | 1.000 |
| 10 | 160.525 | 127.810 | 14.1715 | 72.494 | 38.379 | 2.159 |
| 20 | 232.525 | 160.610 | 19.0915 | 104.531 | 55.340 | 3.030 |
| 30 | 309.525 | 195.410 | 24.3115 | 133.190 | 70.513 | 3.787 |
| 50 | 478.525 | 271.010 | 35.6515 | 185.254 | 98.076 | 5.131 |
| 75 | 717.900 | 376.760 | 51.5140 | 244.607 | 129.498 | 6.627 |
| 100 | 988.525 | 495.010 | 69.2515 | 300.000 | 158.824 | 8.000 |

## Long-horizon multiples

Level 100 / level 1 is currently approximately:

- health: 9.89x,
- primary resource: 4.95x,
- base power: 6.93x,
- reference armor: 9.00x,
- reference resistance: 9.00x,
- rating cost per percentage point: 8.00x.

These values are intentionally restrained for a 100-level game.

They are not meant to make a level-100 character numerically resemble a
different game from a level-1 character.

## CI invariants

The reference suite now rejects changes that exceed these current guardrails:

- health growth above 10x from level 1 to 100,
- primary-resource growth above 5x,
- base-power growth above 7x,
- reference armor/resistance growth above 9x,
- rating-cost growth above 8x,
- any single-level health jump above 7%,
- any single-level resource jump above 4%,
- any single-level power jump above 5%.

Selected level 1/20/50/100 checkpoints are also pinned in tests.

These are regression alarms, not eternal game doctrine. A deliberate rebalance
may update them, but it must do so visibly.

## What this still does not include

This table does not yet contain:

- gear item budgets,
- class health/resource multipliers,
- weapon DPS/attack-speed budgets,
- spell rank base magnitudes,
- elite/boss multipliers,
- profession or consumable bonuses.

Those later layers must be checked against this restrained reference spine
rather than replacing it with exponential inflation.
