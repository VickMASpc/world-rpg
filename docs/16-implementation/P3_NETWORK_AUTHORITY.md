# P3 network authority

Status: ACTIVE

P3 now has a real Fabric play-network transport over the existing
server-authoritative entity-backed combat runtime.

## Client -> server request

The C2S payload contains only:

- monotonically increasing request sequence,
- stable ability ID,
- target Minecraft UUID.

The client does **not** transmit gameplay results.

It cannot authoritatively specify:

- resource cost,
- damage,
- healing,
- cast duration,
- channel interval,
- cooldown,
- range,
- line of sight,
- target validity,
- aura result,
- stat result.

## Server processing

The server:

1. rejects stale/duplicate request sequences,
2. resolves the target UUID against currently loaded living entities,
3. resolves the ability from server-owned definitions/fixtures,
4. binds source and target to server runtime actors,
5. observes current target facts,
6. validates conditions/cooldowns/resources,
7. mutates server RPG state only if accepted,
8. returns a structured response.

There is one gameplay path.

Networking calls the same `P3DeveloperCombatRuntime.handle(...)` used by
server-side developer tools. There is no client combat implementation.

## Server -> client response

The S2C response contains:

- request sequence,
- accepted/rejected,
- whether a timed/channel cast started,
- structured condition failures.

Condition failures preserve stable failure IDs and human-readable developer text.

Future UI may translate/localize those stable IDs without re-evaluating gameplay.

## Replay protection

Each player connection has a monotonically increasing request sequence space.

Equal or older sequences are rejected before gameplay mutation.

Sequence state is cleared on disconnect/server shutdown.

This mechanism protects packet intent ordering. It is not persistence.

## Current client surface

The client currently provides a minimal sender utility and developer feedback
receiver.

It intentionally does not yet expose final action bars or keybind semantics.

The next step is a developer input surface that targets the entity under the
crosshair and sends one of the three P3 fixture ability IDs through this exact
network path.
