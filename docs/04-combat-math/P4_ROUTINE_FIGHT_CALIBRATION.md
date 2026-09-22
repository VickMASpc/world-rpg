# P4 routine same-level fight calibration

Status: FIRST REPRESENTATIVE SCENARIO

P4.4 now has its first end-to-end encounter fixture.

This is not a production class, spell or enemy.

It is a controlled level-20 reference scenario used to answer whether the
current production stack can express the intended routine-combat envelope.

## Production path exercised

The fixture uses:

- World RPG reference level curves,
- reference armor/resistance budgets,
- P4 hit/crit math,
- direct-spell/direct-weapon resolution profiles,
- P3 AbilityDefinition,
- P3 CastController,
- P3 EffectSequence,
- CombatMagnitudeEffect,
- P4 production resolver,
- shared simulator clock,
- defeat events,
- resource-spend metrics,
- seeded batch execution.

There is no simulator-only damage formula or cast timer.

## Reference player

Level: 20.

The player receives:

- reference health,
- reference primary resource as mana,
- reference base power as spell power,
- 5% direct crit,
- reference same-level armor.

Calibration bolt:

- 2.5 second cast,
- 4 mana,
- Arcane,
- direct-spell profile,
- authored base magnitude 10,
- spell-power coefficient from the active math profile.

## Reference routine enemy

The enemy receives:

- 85% of reference same-level health,
- 70% of reference base power as attack power,
- 2% crit,
- reference same-level Arcane resistance.

Routine strike:

- 3.0 second cast/attack cycle,
- Physical,
- direct-weapon profile,
- authored base magnitude 5.

Both actors use source-alive and target-alive P3 conditions. A same-tick kill
therefore invalidates the dead actor's unfinished attack instead of allowing
an ordering artifact to create post-death damage.

## First acceptance neighborhood

Across 256 sequential deterministic seeds, the scenario currently requires:

- average enemy defeat time: 20-35 seconds,
- minimum defeat time: at least 15 seconds,
- maximum defeat time: no more than 45 seconds,
- exactly one defeat event per run,
- average mana spend: 35-65,
- aggregate miss rate: 2-7%.

The wide bands are deliberate.

This first scenario tests the philosophy and pipeline, not final tuning.

## Why mana is intentionally not near-empty

The player begins with the level-20 reference resource pool.

The routine fight is intended to consume only a portion of it.

That preserves the original attrition doctrine:

routine enemy -> routine enemy -> routine enemy -> recovery decision

rather than:

every enemy -> mandatory drink.

Later multi-fight scenarios will test that directly without resetting the
player between enemies.

## What this scenario does not prove

It does not establish:

- level-1 or level-100 TTK,
- final ability-rank progression,
- class-specific health/resource multipliers,
- melee/ranged class balance,
- dodge/parry/block,
- interrupt pressure,
- two-mob pulls,
- elite fights,
- dungeon packs,
- healer stress,
- final item budgets.

It is the first calibrated anchor point, not the final combat model.
