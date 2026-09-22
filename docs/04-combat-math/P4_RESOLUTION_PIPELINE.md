# P4 production magnitude resolution pipeline

Status: TOPOLOGY ACCEPTED — CALIBRATION PROFILE NOT YET FROZEN

The first production resolver is `ProfiledCombatResolver`.

It implements the P3 `CombatResolutionGateway` and is shared by runtime and
future headless simulation.

There is intentionally no canonical default CombatMathProfile yet.

## Resolution order

For DAMAGE or HEALING:

1. authored base magnitude,
2. add power contribution,
3. clamp scaled magnitude at zero,
4. apply outgoing source bonus,
5. determine critical outcome from explicit CombatRollSource,
6. apply critical multiplier when critical,
7. apply mitigation for damage only,
8. apply incoming target bonus/reduction,
9. mutate the configured health resource,
10. compute overkill/overheal excess,
11. emit semantic CombatMagnitudeResolvedEvent,
12. emit generic ResourceChangedEvent.

The resource mutation occurs before either immutable event is returned.

## Power

Current topology chooses:

- Physical DAMAGE -> Attack Power.
- Non-physical DAMAGE -> Spell Power.
- HEALING -> Healing Power.

The profile supplies independent coefficients.

Equation:

```text
afterScaling = max(0, authoredBase + power * coefficient)
```

Primary-stat-to-power conversion is P4.2 work and is not duplicated here.

## Bonuses

Outgoing and incoming modifier stats are represented as additive bonus
fractions where zero is neutral.

```text
multiplier = max(0, 1 + bonus)
```

Examples:

- 0.20 -> 1.20x
- -0.25 -> 0.75x
- -1.00 or below -> 0x

Damage and healing have separate outgoing/incoming bonus stats.

## Criticals

The resolver reads a derived Crit Chance probability.

It clamps that probability to:

```text
[0, profile.maximumCriticalChance]
```

A roll is consumed only when:

- the post-outgoing magnitude is greater than zero, and
- critical chance is greater than zero.

A critical uses the profile's critical multiplier.

Rating -> crit conversion is not defined here.

## Mitigation

Healing does not use damage mitigation.

Damage uses:

- Armor for Physical,
- school resistance for Fire/Frost/Arcane/Nature/Holy/Shadow.

Negative defense is currently treated as zero. Vulnerability belongs to the
explicit incoming damage modifier rather than hidden negative-armor behavior.

Raw mitigation:

```text
defense / (defense + mitigationScale)
```

Final mitigation is clamped to:

```text
[0, profile.maximumMitigation]
```

The profile constants are not yet balance canon.

P4.2 may make mitigationScale level-dependent while preserving this resolution
stage.

## Health mutation

Damage uses partial drain. Healing uses capped gain.

The semantic event records:

- requested final magnitude,
- actually applied magnitude,
- excess.

For DAMAGE, excess is overkill.

For HEALING, excess is overheal.

No presentation rounding occurs in the resolver.

## Event order

After authoritative mutation:

1. CombatMagnitudeResolvedEvent,
2. ResourceChangedEvent.

This gives P4 the owning semantic event for later on-damage/on-heal reaction
categories without turning generic P3 resource changes into damage semantics.

## Still open

- hit/miss/dodge/parry/block,
- absorbs,
- level-dependent mitigation scale,
- final canonical profile constants,
- final damage/healing presentation rounding,
- primary/secondary stat conversions,
- runtime death transition,
- threat from resolved outcomes.
