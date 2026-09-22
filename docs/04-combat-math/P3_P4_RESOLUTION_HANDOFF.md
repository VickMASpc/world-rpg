# P3 -> P4 combat-resolution handoff

Status: ACCEPTED

P3 owns generic combat mechanics and server authority.

P4 owns combat mathematics and simulation.

The boundary is an explicit `CombatMagnitudeRequest` handed to a
`CombatResolutionGateway`.

## P3 request facts

A request contains:

- server game tick,
- DAMAGE or HEALING,
- source CombatActor,
- target CombatActor,
- stable cause ID,
- stable school ID,
- authored base magnitude.

The source/target objects are short-lived runtime references. They are not
persistent identity.

The stable cause/school IDs make the request inspectable and data-driven.

## What authored base magnitude means

It is only formula input.

For example, an authored spell may eventually say that one effect begins with
a base magnitude of 20.

P3 does **not** decide that the target loses 20 health.

## P4 owns

P4 must explicitly decide and test:

- hit/miss when applicable,
- spell/attack power scaling,
- level interaction,
- critical strikes,
- armor,
- resistance,
- vulnerability,
- absorbs,
- healing modifiers,
- final rounding,
- overkill/overheal semantics,
- final health/resource mutation,
- damage/healing outcome events,
- simulator parity.

## No default resolver

P3 intentionally ships no fallback resolver.

This prevents a temporary formula such as:

```text
final damage = authored base amount
```

from quietly becoming production behavior.

The current P3 proof room still uses generic resource-drain effects because it
is testing kernel mechanics, not claiming to be the production damage system.

## Authority

Only logical-server code may invoke the production P4 resolver.

Clients may request abilities and render resolved events, but never submit a
resolved damage/healing amount.
