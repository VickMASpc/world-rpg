# P3 authored combat content

Status: ACTIVE

P3 combat fixtures are no longer constructed by the in-world harness as Java
spell objects.

The P2 server-data transaction owns authored combat definitions and P3 compiles
each successfully published snapshot into executable runtime objects.

## Authored registries

```text
world_rpg:registry/auras
world_rpg:registry/abilities
```

Both currently use schema version 1 and are SAFE for the P3 proof environment.

## Reload path

```text
server datapack JSON
-> strict P2 JSON preflight
-> authored aura/ability registries
-> cross-reference validation
-> candidate combat compilation validation
-> atomic P2 publish
-> P3 compiled combat snapshot swap
```

A rejected P2 candidate replaces neither the active authored snapshot nor the
active compiled combat snapshot.

## Current mechanism schema

Abilities can currently author cast kind, cast/channel timing, cooldown/GCD,
movement policy, typed resource costs, ordered conditions and ordered effects.

Condition types:

- source_alive
- target_alive
- same_world
- disallow_self
- require_self
- line_of_sight
- max_range
- facing_arc
- target_resource_at_least

Effect types:

- drain_resource
- apply_aura

Auras currently author stack cap, duration, uniqueness, refresh policy and
ordered stat modifiers.

Periodic authored aura effects remain a later extension.

## Proof data

Focus, Bolt and Channel now live under:

```text
data/world_rpg/world_rpg/definitions/
```

The developer room still owns ephemeral actor state such as proof mana/health.
It no longer owns ability/aura construction.

The headless P3 mechanism proof may continue constructing isolated generic test
objects because it is a kernel test, not the game content model.

## Boundaries still deferred

- production damage/healing formulas
- real class coefficients
- resource catalogs
- tooltips/localization
- talents modifying definitions
- projectiles
- production reload-safety rules
