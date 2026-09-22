# P3 entity runtime lifecycle

Status: ACCEPTED FOR P3

The entity-backed developer runtime now treats Minecraft entity presence as an
explicit lifecycle boundary.

## Binding

Loaded living entity UUID
-> ephemeral CombatActor
-> runtime-only CombatActorId.

## Unbinding

A binding can be removed by entity UUID.

Removing runtime state also removes the reverse actor-to-entity mapping.

## Developer-room reset

Reset is scoped to the invoking developer's player + owned room target.

It does not clear another developer's target or actor state.

## Pruning

Every 100 server ticks, the P3 developer runtime checks its small set of
currently materialized actor states.

States whose Minecraft living entity can no longer be resolved are removed.

This interval is deliberately coarse:

- no full-world scan,
- no every-tick idle-entity scan,
- stale ephemeral state is bounded,
- active casts are still checked every server tick.

This is integration-harness lifecycle behavior, not the final production actor
store.

## RPG resource exhaustion

The P3 fixture treats positive proof-health as an ability condition.

Once the fixture target's RPG health resource reaches zero, further hostile
fixture activations are rejected even though the invulnerable Minecraft husk
remains physically present.

This proves that RPG combat state can control legality independently from
vanilla Minecraft health without yet implementing P4 damage/death math.
