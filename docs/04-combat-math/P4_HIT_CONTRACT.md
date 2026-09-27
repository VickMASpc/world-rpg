# P4 hit/contact contract

Status: FIRST CONTACT LAYER IMPLEMENTED

P4 now distinguishes **resolution semantics** from damage school.

A request carries a stable `resolutionProfileId`.

The school still answers questions such as:

- which resistance applies,
- which school-specific modifiers apply.

The resolution profile answers contact/critical questions.

This prevents false assumptions such as:

```text
Physical => melee => parryable
Fire => spell => must roll spell miss
```

Neither implication is safe.

## Current stable profile identities

```text
world_rpg:resolution/guaranteed
world_rpg:resolution/direct_weapon
world_rpg:resolution/direct_spell
world_rpg:resolution/periodic
```

P3 carries the ID but does not interpret its mathematics.

P4 maps the ID to the active CombatOutcomeProfileSet.

## Current contact equation

The first contact layer supports HIT / MISS.

For a profile with a baseline miss chance:

```text
missChance =
    clamp(
        baseMissChance - sourceHitChance,
        0,
        1
    )
```

Hit chance is a derived additive fraction.

Examples:

```text
0.03 = +3 percentage points of hit
0.05 base miss - 0.03 hit = 0.02 final miss
```

When final miss chance is zero, no contact RNG roll is consumed.

When the result is MISS:

- magnitude scaling does not run,
- critical RNG does not run,
- health does not mutate,
- CombatMagnitudeResolvedEvent is still emitted,
- ResourceChangedEvent is not emitted.

This ordering is deterministic and regression-tested.

## Current P4 draft calibration

Current provisional baselines:

```text
guaranteed:    0% miss, may crit
direct weapon: 5% miss, may crit
direct spell:  4% miss, may crit
periodic:      0% miss, does not crit
```

These percentages are calibration drafts, not frozen doctrine.

## Compatibility path

The older CombatMagnitudeRequest constructor does not contain an explicit
profile ID.

It delegates to:

```text
world_rpg:resolution/guaranteed
```

This preserves the existing P3 proof/resource-drain semantics without guessing
what kind of attack old callers intended.

New production magnitude effects should carry an explicit profile ID.

## Why dodge/parry/block are not implemented in this tranche

Those outcomes require more than a damage school and a target stat.

For example, parry may depend on:

- attack family,
- target-facing relationship,
- target state,
- weapon/shield state.

Block has similar eligibility questions.

The current magnitude request does not yet carry those runtime eligibility
facts.

P4 will not fake them.

A later tranche must carry explicit eligibility/context from authoritative
target observation before Dodge/Parry/Block are consumed.

## Numeric semantics

All contact probabilities and magnitude calculations remain full-precision
finite doubles.

No gameplay intermediate is rounded for UI presentation.

Presentation may format values independently; it does not feed rounded values
back into authoritative state.
