# P4 deterministic batch simulation

Status: CORE BATCH RUNNER IMPLEMENTED

P4 can now execute the same scenario across an explicit set of deterministic
seeds.

## Contract

A CombatSimulationScenario is:

```text
seed -> CombatSimulationReport
```

The batch runner does not implement combat.

It does not own RNG.

It does not reset actors.

It only supplies explicit seeds and collects immutable reports.

The scenario is responsible for constructing the normal production resolver,
actors and decision driver.

## Seed control

Two entry points exist:

```text
runSeeds(scenario, explicitSeeds)
runSequential(scenario, firstSeed, runCount)
```

Explicit seed order is preserved, including repeated seeds.

Sequential runs use:

```text
firstSeed
firstSeed + 1
firstSeed + 2
...
```

with overflow checked.

This makes a failed calibration run exactly reproducible.

## Report additions

Single-run reports now distinguish:

- damage contacts that hit,
- damage contacts that miss.

The invariant is:

```text
damageHits + damageMisses = damageResolutions
```

The batch report currently exposes:

- run count,
- average/min/max elapsed ticks,
- average applied damage,
- average applied healing,
- aggregate damage miss rate,
- average critical-resolution count,
- average explicit recovery by resource.

These are infrastructure metrics.

TTK/death, resource spend, downtime and interruption pressure require the later
ability/decision driver rather than pretending raw magnitude requests are full
encounters.

## Why batches matter

One deterministic example proves mechanics.

It does not prove balance.

P4 calibration needs distributions across many seeds so we can detect:

- excessive miss/crit variance,
- unstable TTK tails,
- resource-pressure outliers,
- pathological elite/boss outcomes,
- balance regressions after formula changes.

The same seed set can be rerun before and after a change and compared exactly.
