# Data model and identity

Status: PROPOSED

## Stable IDs

Cross-content references use namespaced IDs such as:

- world_rpg:ability/mage/frostbolt
- world_rpg:aura/frosted
- world_rpg:item/redvale/ironwood_staff
- world_rpg:quest/redvale/missing_caravan/03
- world_rpg:zone/redvale/old_orchard

Released IDs are durable persistence keys. Renaming requires migration/alias support.

## Definition versus state

Definitions are immutable loaded data. Instances/state are mutable play data referencing definitions.

An item definition owns slot, requirements, base stats, rarity and visuals. An item instance owns durability, binding and any permitted rolled/added state.

The same separation applies to abilities, quests, NPCs, mobs, recipes and world objects.

## Load lifecycle

source -> decode -> structural validation -> candidate registries -> reference resolution -> semantic/cross-registry validation -> atomic publish.

Missing mandatory references are errors, never silent nulls.

## Schema versions

Persisted state and generated formats carry explicit schema versions. Changes are compatible-additive, migratable-breaking or non-migratable-breaking.

## Semantic locations

Designed places receive IDs. Quests/world logic reference those IDs rather than embedding coordinates.

## Reload policy

Domains will be classified SAFE, GUARDED or RESTART for live reload. The loader must never pretend every data edit is safe against active runtime state.
