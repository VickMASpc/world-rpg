# P4 reusable ability decision policy

Status: MINIMAL PRODUCTION-VALIDATED PRIORITY POLICY IMPLEMENTED

Headless scenarios no longer need to probe abilities by attempting mutation.

CastController now exposes validateActivation(ability, target, tick).

The method performs the same production preflight used by tryActivate:

- already-casting state;
- ability cooldown;
- global cooldown;
- authored activation conditions;
- effect-sequence preflight;
- resource availability.

It does not spend resources, start cooldowns, allocate a cast ID, apply effects or mutate active-cast state.

PriorityAbilityPolicy then walks abilities in deterministic authored order and returns the first ability whose production preflight passes.

This is intentionally a small policy layer, not an AI framework.

It is enough for upcoming elite/dungeon simulations to express patterns such as:

1. use emergency/required action when legal;
2. use efficient primary attack while resource permits;
3. fall back to zero-cost attack;
4. otherwise wait.

Target selection and encounter-specific decisions remain separate. The policy does not own game rules; it asks CastController for them.