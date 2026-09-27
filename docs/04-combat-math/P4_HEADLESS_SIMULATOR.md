# P4 headless combat simulator

Status: CORE SHELL IMPLEMENTED

The headless simulator wraps the same production CombatResolutionGateway used
by runtime combat.

It does not own a duplicate damage/healing formula.

## Clock

SimulationClock advances integer game ticks deterministically.

Advancing time has no gameplay side effect by itself.

In particular, time does not:

- refill health,
- refill mana/resources,
- clear auras,
- reset cooldowns through a simulator shortcut,
- create a fresh actor for the next fight.

Later systems may explicitly process aura/cooldown/regen ticks, but those are
mechanics, not an encounter-reset assumption.

## Persistent actor state

The caller provides CombatActor instances.

The simulator keeps using those same instances across resolutions.

Therefore multi-fight attrition is natural:

```text
fight
-> travel / elapsed time
-> fight
-> explicit recovery
-> fight
```

rather than:

```text
fight
-> hidden full reset
-> fight
```

## Explicit recovery

`recover(actor, resource, amount)` is an explicit simulation action.

It uses the same ResourcePool gain semantics as runtime state and emits a
ResourceChangedEvent.

The simulation report records applied explicit recovery by resource.

This is only the generic shell for later food/drink/rest mechanics.

## Journal

Every recorded CombatEvent is retained in deterministic order.

The same journal can later receive:

- CastController events,
- aura events,
- cooldown events,
- recovery events,
- resolved damage/healing events.

This allows a future scenario driver to compose the P3 kernel with P4
mathematics rather than replacing either.

## Current metrics

The report currently records:

- elapsed ticks,
- event count,
- damage/healing resolution count,
- critical resolution count,
- applied damage,
- applied healing,
- overkill,
- overheal,
- explicit recovery by resource.

TTK, resource spend, downtime and interrupt pressure are added when the
simulator begins driving full abilities/casts rather than raw magnitude
requests.
