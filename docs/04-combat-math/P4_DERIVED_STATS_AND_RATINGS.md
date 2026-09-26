# P4 derived stats and secondary ratings

Status: MECHANISM ACCEPTED — CALIBRATION DRAFT

P3 owns raw runtime stats and modifiers.

P4 now reads those through a CombatStatResolver.

This creates a clean boundary between:

```text
raw/base/modifier state
→ deterministic derived-stat conversion
→ combat formulas
```

without copying derived numbers back into the P3 StatSheet.

## Direct values remain valid inputs

A derived target stat begins with its direct StatSheet value.

Profiles may add:

- linear raw-stat contributions,
- level-aware rating contributions.

This allows innate/class values, auras or explicit effects to modify the
derived target directly while ordinary equipment ratings are converted through
the P4 profile.

## Primary -> derived policy

There is deliberately **no universal Strength -> Attack Power or Intellect ->
Spell Power equation in the kernel**.

Class/loadout systems will provide conversion profiles.

A linear rule is:

```text
derived += rawSourceStat * coefficient
```

Multiple raw primary stats may contribute to one target.

Source stats are always read directly from the P3 StatSheet. Derived rules do
not recursively consume other derived rules.

That one-layer rule is intentional:

- no hidden cycles,
- no formula-order dependence,
- no accidental derived-stat feedback loops.

If a future design genuinely needs multi-stage derivation, it must define an
explicit ordered pipeline rather than relying on recursion.

## Secondary rating conversion

A rating curve defines how many points are required for one percentage point at
a given level.

```text
p = (level - 1) / (levelCap - 1)

ratingPer1Pct(level) =
    lowCost
    + (highCost - lowCost) * p^exponent

derivedFraction =
    rating / ratingPer1Pct(level) * 0.01
```

The conversion itself does **not** clamp crit/hit/dodge/etc.

Caps and diminishing returns belong to the semantic consumer/outcome model.
This avoids hiding two independent balance systems inside one conversion.

## Reference P4 draft

The current simulator-only reference curve is:

```text
level cap: 100
rating for 1% at level 1:   1
rating for 1% at level 100: 8
growth exponent:            0.75
```

The same draft curve is currently attached to:

- crit rating -> crit chance,
- hit rating -> hit chance,
- haste rating -> haste bonus,
- dodge rating -> dodge chance,
- parry rating -> parry chance,
- block rating -> block chance.

These equal costs are **not gear-budget canon**.

They give the simulator a coherent scaling language before item budgets and
class roles exist.

For example:

```text
8 rating at level 1   = 8%
8 rating at level 100 = 1%
```

That prevents a fixed item rating from becoming more valuable simply because a
character levels.

## Haste representation

Haste is represented as an additive bonus fraction:

```text
0.10 = +10% haste
```

The cast/cooldown cadence formula that consumes this value remains separate
P4/P3 integration work.

## Important unresolved dependency: hit semantics

P4 now has derived Hit/Dodge/Parry/Block chances, but the magnitude request does
not yet say whether an authored effect is:

- melee,
- ranged,
- spell,
- periodic,
- unavoidable,
- blockable,
- dodgeable,
- parryable.

P4 must **not infer those semantics from damage school**.

Physical does not automatically mean melee.

The next hit-table tranche therefore requires an explicit authored
resolution-profile/attack-trait identity before those chances are consumed.

## Calibration status

The profile mechanism is production architecture.

The numerical reference curve is calibration data and may change when:

- gear budgets exist,
- class profiles exist,
- same-level encounter scenarios exist,
- item replacement cadence is simulated.
