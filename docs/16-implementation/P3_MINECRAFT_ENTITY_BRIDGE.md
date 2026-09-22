# P3 Minecraft entity bridge

Status: ACTIVE

The P3 branch now has a deliberately narrow Minecraft integration layer.

## Identity rule

Minecraft's transient numeric entity ID is **not** RPG identity.

Current server-lifetime binding uses:

```text
Minecraft living-entity UUID
        ->
ephemeral CombatActor
        ->
runtime-only CombatActorId
```

This does not declare Minecraft UUID to be persistent World RPG character identity.

Character persistence remains a P2 decision.

The binding index is cleared when the Minecraft server stops.

## Target probe

Developer command:

```text
/worldrpg p3 probe <target>
```

For a living command source and living target it reports raw integration observations:

- source runtime actor ID,
- target runtime actor ID,
- whether source and target are the same entity,
- whether they are in the same world,
- source alive state,
- target alive state,
- line of sight,
- physical distance when in the same world.

## Conservative boundary

The probe **does not decide target legality**.

It intentionally does not say:

- maximum spell range,
- hostile/friendly legality,
- whether line of sight is required,
- whether dead targets are legal,
- facing arcs,
- PvP rules,
- faction relationships.

Those belong to ability/targeting policy and later authored definitions.

The Minecraft adapter observes facts.

The RPG kernel and data decide what those facts mean.

## Next integration

- formal target snapshot contract consumed by activation conditions,
- movement-state observation,
- facing observation,
- entity-binding cleanup beyond whole-server shutdown,
- real player/hostile actor state initialization,
- client-to-server activation requests,
- actual developer combat room.
