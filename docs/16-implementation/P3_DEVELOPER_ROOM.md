# P3 developer combat room

Status: ACTIVE

P3 now has an entity-backed in-world integration harness.

It is deliberately called a "room" even though the current implementation is
a lightweight lane around the developer's current position. Building permanent
map geometry here would confuse integration testing with world production.

## Commands

```text
/worldrpg p3 room spawn
/worldrpg p3 room spawn <distance>
/worldrpg p3 room status
/worldrpg p3 room reset
```

Default spawn distance is 3 blocks.

Explicit distance accepts 1–30 blocks.

## Target

The harness spawns a real HuskEntity:

- AI disabled,
- persistent while the harness owns it,
- Minecraft damage invulnerable,
- visible custom name: `P3 RPG Target`.

Minecraft invulnerability is intentional.

P3 fixture abilities currently mutate the target's RPG HEALTH resource rather
than vanilla health. Normal swords/arrows must not contaminate this proof.

The target is discarded on harness reset/replacement and on server shutdown.

## Why distance is configurable

Fixture abilities currently demonstrate different range limits.

The room can intentionally create:

- a clearly valid close-range target,
- a target near the limit,
- a target outside the limit.

This lets us falsify range behavior rather than only confirming the happy path.

## Current manual proof flow

1. Run `/worldrpg p3 room spawn 3`.
2. Look at the named target.
3. Press F6 and verify self Focus is accepted.
4. Press F7 and stand still through the Bolt cast.
5. Use `/worldrpg p3 room status` and confirm:
   - player mana decreased,
   - player power reflects the item + aura,
   - target RPG health decreased.
6. Press F7 again and move during the cast; verify MOVEMENT interruption.
7. Spawn/reset the target at a distance beyond Bolt's fixture range and verify
   a structured rejection.
8. Put a wall between player and target and verify LOS rejection.
9. Press F8 and verify three scheduled channel ticks when uninterrupted.
10. Reset and verify the spawned target is removed and session state is cleared.

## What this proves

Once manually exercised, this harness tests the complete path:

```text
physical Minecraft world
-> real living source/target
-> client developer input
-> Fabric C2S payload
-> replay guard
-> server entity resolution
-> server target observations
-> RPG conditions/resources/cooldowns
-> server cast lifecycle
-> generic effects
-> RPG actor state
-> structured S2C response
```

## What remains deliberately outside

- final combat math,
- vanilla-health damage integration,
- factions/hostility,
- final targeting controls,
- final action bars,
- final UI/VFX,
- authored class content,
- actual world/dungeon geometry.

Those are later gates.
