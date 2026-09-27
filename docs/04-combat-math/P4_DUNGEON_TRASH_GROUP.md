# P4 dungeon trash group calibration

Status: FIRST SMALL-PARTY GROUP SCENARIO

This scenario is the first headless P4 fixture with multiple friendly roles and
multiple enemies acting concurrently.

## Party

The level-20 reference party contains:

### Tank
- 2x reference health;
- 1.5x reference armor;
- zero-cost three-second Physical strike.

### Healer
- reference mana and Healing Power;
- 2.5-second heal;
- heal becomes legal only when tank health is at/below 70%;
- 10 mana per heal.

### Caster DPS
- reference mana and Spell Power;
- 2.5-second Arcane bolt costing 4 mana;
- zero-cost four-second fallback after mana exhaustion;
- instant InterruptEffect ability for the dangerous caster spell.

## Enemy pack

### Guard
- 2x reference health;
- durable Physical pressure.

### Skirmisher
- reference health;
- faster lighter Physical pressure.

### Caster
- reference health;
- ordinary Shadow bolt;
- dangerous four-second Shadow cast on a twenty-second cooldown.

DPS target priority is:

caster -> skirmisher -> guard.

Tank target priority is:

guard -> skirmisher -> caster.

This creates overlapping target responsibilities rather than making every
friendly actor attack the same enemy by hard-coded simulator rule.

## Threat limitation

Production threat is not connected to this fixture yet.

All three enemies deliberately target the tank.

That fixed target choice is an explicit temporary harness boundary, not a claim
that threat/tanking is finished.

When P4 threat integration arrives, this scenario should replace the fixed
target selection with the production threat table while preserving the rest of
the encounter.

## Production systems exercised

- shared deterministic RNG;
- several concurrent CastController instances;
- explicit power scaling;
- armor/resistance;
- hit/miss/crit;
- resource costs;
- ResourceConditions health threshold;
- PriorityAbilityPolicy;
- InterruptEffect through cast-control gateway;
- target-invalid cast cancellation after enemy death;
- healing and damage through the same P4 resolver;
- semantic defeat events;
- persistent party resources;
- zero hidden recovery.

## Acceptance neighborhood

Across 256 deterministic seeds:

- pack duration should remain roughly 90-150 seconds;
- all three enemies should be defeated in nearly every run;
- tank death should remain rare;
- at least one dangerous caster spell should normally be interrupted;
- combined healer + DPS mana spend should show meaningful pressure;
- no explicit mana recovery is allowed.

A representative seed must:

- clear all three enemies;
- force the DPS into its zero-cost fallback;
- leave the healer with some reserve;
- leave the tank alive.

## Design meaning

This is not a boss simulation.

It is meant to feel like a substantial dungeon trash pull: long enough for role
coordination and resource pressure to matter, but short enough that a dungeon
can contain many such pulls without every group becoming a boss encounter.

The fixture also proves that the simulator can now model multiple friendly and
hostile actors using the same production kernel rather than a special
single-player combat loop.
