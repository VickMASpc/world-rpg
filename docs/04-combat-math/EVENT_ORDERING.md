# P3 combat event ordering

Status: PROPOSED

This document records the ordering already enforced by the P3 kernel and the boundaries still intentionally missing.

## Ability activation

Before any mutation:

1. reject if another cast is active,
2. check ability cooldown,
3. check global cooldown,
4. evaluate activation conditions,
5. validate effect preconditions,
6. aggregate resource costs by resource type,
7. verify every aggregated cost can be paid.

Only after all activation validation passes:

1. emit ability-activated event,
2. spend resources in deterministic cost order,
3. emit resource-change events,
4. start ability cooldown,
5. emit ability-cooldown event,
6. start global cooldown,
7. emit global-cooldown event,
8. either resolve an instant effect sequence or create a cast.

A rejected activation mutates nothing.

## Timed cast completion

At completion:

1. re-evaluate activation/target conditions,
2. revalidate effect preconditions,
3. if invalid, interrupt/fizzle without applying effects,
4. otherwise end the active cast,
5. emit cast-completed,
6. resolve effects in declared order.

Resource costs and cooldowns are not implicitly refunded by interruption.

Refund policy, if needed, must become explicit ability data rather than hidden kernel behavior.

## Channel

At each scheduled channel interval:

1. revalidate,
2. resolve the effect sequence,
3. emit the sequence's events,
4. schedule the next interval.

When total channel duration ends, emit cast-completed.

A delayed server tick processes missed scheduled channel intervals in deterministic order up to the channel end.

## Aura mutation

Applying an aura:

1. find the matching runtime instance according to uniqueness policy,
2. create or update stacks,
3. update duration according to refresh policy,
4. recompute aura-owned stat modifiers,
5. emit aura-applied.

Removing an aura:

1. remove runtime instance,
2. remove modifiers owned by that exact instance,
3. emit aura-removed when removal came through an effect.

Expiry currently returns explicit removal records; the higher-level combat tick/event dispatcher still needs to convert expiry into the shared event stream.

## Still missing from P3.6

The project does not yet have a general proc/reaction dispatcher.

Before procs are implemented we must define:

- event phases that listeners are allowed to observe,
- deterministic listener priority,
- whether a listener may mutate the triggering event,
- child-event sequencing,
- recursion/re-entry limits,
- loop detection,
- maximum trigger depth,
- exact semantics of on-cast vs on-complete vs on-hit vs on-damage.

No proc system should be added ad hoc before these are explicit.
