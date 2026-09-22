# P3 server authority boundary

Status: ACCEPTED

The entity-backed P3 integration uses a minimal activation request:

```text
request sequence
ability ID
target Minecraft UUID
```

That is intent, not authority.

## The requester does not provide

- mana/resource cost,
- cooldown state,
- cast duration,
- channel interval,
- range,
- line of sight,
- target alive state,
- damage,
- healing,
- aura duration,
- stat result,
- hit success.

The logical server resolves all of those from its current RPG definitions, runtime state, and Minecraft observations.

## Entity-backed developer state

The P3 developer runtime now owns ephemeral combat sessions for actual living Minecraft entities.

It gives fixture actors:

- RPG health resource,
- RPG mana resource,
- power stat,
- developer-only player item modifier,
- server-side CastController,
- fresh Minecraft target observation provider.

This state is **not persistence**. It resets with the developer runtime/server lifecycle.

P2 remains the owner of durable player/world state design.

## Movement interruption

When a timed/channel cast begins, the developer runtime records the source entity's position.

Any physical displacement beyond a tiny numeric tolerance interrupts that cast with MOVEMENT before resolution.

This is a mechanism proof.

Final abilities may later choose movement policies; P3 is proving that server-owned movement interruption works.

## Fresh target legality

At activation and again at timed/channel resolution, target conditions read fresh server observations.

The fixture hostile abilities currently demonstrate:

- source alive,
- target alive,
- same world,
- no self target,
- line of sight,
- maximum range.

The fixture ranges and durations are test values, not final balance decisions.

## Next boundary

The next step is to serialize the same request through Fabric networking.

The server networking handler must call this same runtime path rather than introducing a second combat implementation.
