# Combat doctrine

Status: ACCEPTED FOR P3

P3 builds mechanisms. P4 tunes mathematics. Class content comes later.

The kernel must make the intended combat life possible without prematurely deciding final health curves, damage coefficients, armor formulas, regeneration rates, crit values, or class rotations.

## Experience

Combat gives texture to living in the world.

Routine fights may be calm and readable. Difficult pulls, packs, elites, dungeons and bosses progressively demand more attention. Several fights may leave the player low enough on health or mana that recovery becomes a real decision.

The game should support the thought:

> I can do another pull, but I probably should drink first.

## Rhythm

Routine pull: comfortable, familiar, perhaps one meaningful reaction.

Difficult pull: resource management, target priority, interrupt, positioning, control or cooldowns matter.

Pack: multiple enemies change the problem through assist behavior, target choice and limited resources.

Elite: commitment increases and mistakes have time/resource consequences.

Dungeon/boss: the same generic systems scale upward instead of switching to a different combat language.

## Casting

Casting should have weight.

The engine must support instant actions, timed casts, channels, interruption, movement restrictions where requested, target/range legality, explicit resource timing and cooldowns.

Standing still to finish something powerful can be the point.

## Resources

Resources persist across encounters unless an explicit rule restores them.

The kernel must not silently reset actors to full after each fight.

P3 supplies storage/spend/gain/max-change mechanics. P4 supplies regeneration rates and balance.

## Stats

Stats represent accumulated history from equipment, auras, progression, buffs, debuffs and future talents.

The engine needs explicit sources, removable modifiers, deterministic ordering and dirty recomputation rather than global per-tick scans.

## Conditions

Ability legality should be explainable.

A failed action should be able to say why: insufficient resource, wrong target, movement, missing equipment, range, aura/state requirement, and so on.

## Effects and auras

Abilities should compose generic effects rather than require spell-named Java classes.

Auras are persistent state with explicit source, target, duration, stacks, application, refresh, tick and removal lifecycle.

## Failure modes to resist

A technically correct kernel still fails if it naturally pushes us toward:

- perfect resource resets after every fight,
- every action being instant,
- constant proc/noise spam,
- class-specific assumptions in generic code,
- hidden event ordering,
- accidental explosive modifier stacking,
- per-tick global scans,
- combat that requires permanent maximum attention.

## Observable proof

The P3 proof slice is helping when:

- one ordinary target can be fought calmly,
- three sequential fights visibly consume persistent resources,
- recovery changes the next decision,
- one timed cast differs meaningfully from an instant action,
- one aura changes runtime state generically,
- one item modifier uses the same stat engine as an aura,
- failed activations report structured reasons,
- changing ability data changes behavior without adding a spell class,
- deterministic tests replay the same sequence with the same state transitions.
