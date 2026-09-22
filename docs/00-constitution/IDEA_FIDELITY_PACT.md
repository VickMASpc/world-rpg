# Idea fidelity pact

Status: FROZEN

World RPG is large enough that accidental drift is a greater threat than lack of ideas.

This pact establishes a conservative development posture:

**Preserve the idea unless there is a demonstrated reason to change it.**

Progress is not measured by code volume, feature count, convenience, modernity, or implementation speed. Progress is measured by how faithfully the implementation increases our ability to produce the intended lived experience.

## The pact

We will prefer continuity over novelty.

We will prefer explicit change over silent drift.

We will prefer a slower correct implementation over a faster implementation that changes the game.

We will not adopt a feature merely because it is standard in modern games, easy in Minecraft, easy in Fabric, easy in a library, fashionable, or technically impressive.

We will not remove friction merely because friction exists. Some friction is intentional: distance, preparation, recovery, commitment, imperfect navigation, slow acquisition, and the need to return to civilization.

We will not preserve friction merely because it is old-school. Friction that wastes time without producing memory, decisions, rhythm, tension, attachment, or meaning is padding and should be challenged.

We will not let engine limitations quietly redefine the design. When Minecraft resists the idea, the first question is whether the engine can be adapted—not whether the idea should be discarded.

We will not let architecture become the product. Infrastructure is valuable only because it protects and enables the game.

We will not let scale become spectacle. More levels, items, quests, land, particles, buttons, systems, or enemies are only worthwhile when they increase depth, continuity, memory, or lived-in texture.

## Constitutional invariants

These are the strongest current invariants. Changing one requires an explicit amendment document or ADR that names the invariant being changed and why.

### The world is a place, not a menu

- Geography remains meaningful.
- First journeys remain physical.
- Repeat travel improves through learned/discovered infrastructure.
- Fast travel does not collapse the continent into destinations.
- Regions remain useful across broad spans of character progression.

### Leveling is the game

- The 1–100 journey is not a tutorial for endgame.
- Progress is slow overall but readable locally.
- Levels and gear do not inflate so quickly that recent history becomes irrelevant.
- The player's character accumulates history rather than continually replacing it.

### Slow means lived-in, not padded

- Time investment should come from travel, exploration, combat, quests, professions, preparation, recovery, discovery, and return.
- Empty repetition added only to hit an hour target is not acceptable.
- The game respects the player's time even while deliberately consuming a lot of it.

### Combat breathes

- Routine combat can be calm.
- Difficult pulls matter because they contrast with calm ones.
- Resources may persist across fights.
- Casting can require commitment.
- The kernel must support deliberate play without forcing permanent high APM.

### Civilization matters

- Towns, inns, trainers, merchants, transport, repair, storage, and professions are part of the adventure loop.
- Expeditions should create reasons to leave and reasons to return.
- Field convenience must not make settlements obsolete.

### Authored memory matters

- Places, equipment, enemies, NPCs, quest chains, and routes should be able to remain relevant long enough to become remembered.
- Disposable five-hour zones and disposable fifteen-minute items are not the default production model.

## Change protocol

A proposed change that touches an invariant must answer:

1. Which invariant is affected?
2. What player problem are we solving?
3. Why can the problem not be solved while preserving the invariant?
4. What experience is lost?
5. What experience is gained?
6. Is the change reversible?
7. What existing systems/content need migration?
8. What playtest evidence would justify keeping the change?

If those questions cannot be answered, the default is **do not change the invariant**.

## Conservative review rule

When choosing between two viable implementations:

- prefer the one that preserves more future options,
- prefer the one that encodes less accidental design,
- prefer the one that keeps authored content inspectable,
- prefer the one that keeps state migratable,
- prefer the one that makes system behavior explicit,
- prefer the one that does not force convenience into the player's experience,
- prefer the one that can be tested against the original doctrine.

## Progress review

At every major gate we ask two separate questions:

### Technical progress

Did we make the plant more capable, safe, testable, debuggable, or scalable?

### Idea progress

Did this move us closer to the intended player life?

A technically successful gate can still be rejected if it advances the wrong game.

## Final authority

The constitution, spirit documents, accepted doctrines, and explicit user decisions outrank implementation convenience.

When code and idea disagree, code changes first.
