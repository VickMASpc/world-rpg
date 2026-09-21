# Runtime architecture

Status: PROPOSED

World RPG begins as a modular monolith: one runtime mod artifact with strict internal subsystem ownership.

## Planned common packages

api/id, api/registry, bootstrap, character, combat, content, quest, dialogue, npc, mob, loot, economy, profession, reputation, world, persistence, network, command, debug and integration.

Combat further separates ability, cast, condition, effect, aura, targeting, damage, resource and threat.

## Planned client packages

hud, screen, input, render, model, animation, vfx, audio, map and client debug.

## Dependency direction

Low-level identity/registry primitives feed stat/condition primitives, then effects/auras/resources, then casts/abilities/combat, then character/mob/quest/world systems, then presentation.

High-level domains should interact through defined services/events instead of arbitrary cross-package reach-through.

## Server authority

The logical server owns health, resources, casts, cooldowns, target legality, damage/healing, auras, threat, death, XP, levels, currencies, inventory/equipment mutation, loot, quest state, reputation, professions, travel unlocks and world flags.

The client submits intents and renders synchronized results.

## Explicit ordering

P3 must define deterministic pipelines for activation, cast completion/interruption, hit resolution, damage, death, aura lifecycle, proc dispatch and quest-event consumption. Accidental Fabric callback order is not game design.

## Performance

Avoid per-tick scans of all definitions or all actors. Active runtime state owns timers and scheduled work.

## Debugging

Definitions and live player/entity/cast/aura/quest/item state must be inspectable by developer tools without attaching a Java debugger.
