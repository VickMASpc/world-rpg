# P3 combat event ordering

Status: ACCEPTED FOR CURRENT P3 MECHANISMS

Combat state changes produce immutable events.

Events are facts, not suggestions. A later reaction cannot rewrite the event that caused it.

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

## Reaction topology

The reaction pipeline is deliberately conservative.

1. Gameplay mutation emits immutable root events.
2. Root events receive monotonically increasing sequence numbers.
3. Listeners observe events only; listener code is contractually pure.
4. Listener order is deterministic:
   - lower numeric priority first,
   - registration order breaks priority ties.
5. Listeners return immutable reaction requests.
6. Only the reaction executor may perform gameplay mutation.
7. Mutation emits child events.
8. Child events record their parent sequence and causal depth.
9. Child events are processed breadth-first.
10. Maximum depth is checked **before** a deeper reaction executes.
11. Maximum total events per dispatch is bounded.
12. Duplicate listener IDs are forbidden.

This structure deliberately refuses mutable PRE-event interception during P3.

A reaction may respond to a fact, but it may not retroactively change whether that fact occurred.

If future mechanics genuinely require prevention/replacement (for example an absorb preventing damage), that belongs in the owning resolution pipeline before the immutable event is emitted, not in a generic event listener.

## Still intentionally unresolved

Event topology does not yet define final semantic proc categories such as:

- on cast requested,
- on cast started,
- on cast completed,
- on hit,
- on damage attempted,
- on damage dealt,
- on heal,
- on aura applied,
- on interrupt.

Those names must follow the final owning pipelines instead of being invented ahead of damage/healing resolution in P4.
