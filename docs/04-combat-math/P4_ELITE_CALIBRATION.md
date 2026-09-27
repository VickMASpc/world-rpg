# P4 elite guardian calibration

Status: FIRST LONG-FORM ELITE SCENARIO

The elite scenario is intentionally not a routine enemy with only a health
multiplier.

It tests the first sustained combat loop long enough for several systems to
matter together.

## Reference elite

Level 20.

The elite has:

- four times reference health,
- reference attack power,
- reference Arcane resistance,
- a low-pressure four-second basic attack,
- a dangerous four-second Shadow cast on a thirty-second cooldown.

The dangerous cast is interruptible.

## Reference player decision loop

The player has three functional choices:

1. interrupt the dangerous cast when a reaction window opens;
2. cast the mana-costing 2.5-second Arcane bolt while enough mana remains;
3. fall back to a zero-cost four-second Arcane attack after mana is exhausted.

Normal attack selection uses PriorityAbilityPolicy, which asks the production
CastController preflight rather than duplicating resource/cooldown rules.

The interrupt remains an actual instant AbilityDefinition containing
InterruptEffect.

## Acceptance neighborhood

Across 256 deterministic seeds the first elite target is:

- average fight duration: 2-5 minutes;
- average mana spend: approximately the full level-20 reference pool;
- at least several successful interrupt events per fight;
- elite defeated in the strong majority of seeds;
- player death remains possible but is not the normal outcome.

A representative fixed-seed run must explicitly contain zero-cost fallback
AbilityActivatedEvent entries after mana pressure prevents more bolt casts.

## Design purpose

This scenario tests a different combat rhythm from routine leveling:

- resource exhaustion can happen;
- exhaustion changes the rotation rather than ending the fight;
- dangerous casts periodically demand attention;
- long duration does not require arbitrary resource refills;
- a durable enemy still uses the same production cast/effect/math pipeline.

## What remains outside this scenario

- player defensive cooldowns;
- shield/absorb storage;
- crowd control;
- dodge/parry/block;
- threat/tanking;
- multi-player group roles;
- boss phase scripting.

Those belong to later calibration layers rather than being hidden inside the
first elite fixture.
