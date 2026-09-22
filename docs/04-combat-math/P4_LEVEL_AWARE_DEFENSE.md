# P4 level-aware armor and resistance

Status: MECHANISM ACCEPTED — REFERENCE BUDGET DRAFT

The production resolver now supports a level-scaled mitigation denominator.

## Formula

Damage mitigation remains:

```text
mitigation =
    defense / (defense + mitigationScale)
```

then clamped by the profile's maximum mitigation.

The important change is that `mitigationScale` may now vary by progression
level.

The resolver uses the **attacker/source level**.

This gives level difference a direct but gradual interaction with defense:

- same-level reference armor can hold a stable mitigation target,
- an older armor budget remains useful,
- a much higher-level attacker cuts through that old defense more efficiently,
- raw health/armor values do not need exponential inflation to create level
  relevance.

## Why source level

Using attacker level means the meaning of a fixed armor value changes gradually
against stronger opponents.

That supports the world doctrine:

- a player ten levels above an area should feel stronger,
- the old area should not instantly become numerically meaningless,
- large level gaps eventually matter strongly.

This is calibration-sensitive and will be exercised in P4 scenario batches.

## Reference scale draft

Current simulator scale:

```text
level 1:   100
level 100: 900
growth exponent: 0.80
```

This is approximately the same order of long-horizon growth as the reference
health curve.

## Reference same-level budgets

Current reference targets:

```text
armor:      25% same-level physical mitigation
resistance: 15% same-level school mitigation
```

Required defense is derived from the desired mitigation:

```text
defense =
    scale * mitigation / (1 - mitigation)
```

Therefore reference armor is approximately:

```text
level 1:   33.3
level 100: 300
```

and reference resistance approximately:

```text
level 1:   17.6
level 100: 158.8
```

These are **reference actor budgets**, not final armor-item tables.

## Still unresolved

- armor distribution across equipment slots,
- heavy/light/no-armor class multipliers,
- enemy armor packages,
- resistance rarity,
- penetration stats,
- debuffs such as armor reduction,
- boss-specific mitigation,
- exact level-difference feel at +5/+10/+20/+30 levels.

Those require simulator scenarios and later item/class budgets.
