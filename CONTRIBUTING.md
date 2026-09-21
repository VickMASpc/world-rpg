# Contributing to World RPG

World RPG is in architecture-first pre-production. Changes should preserve the separation between runtime systems, authored content, generated output, and source assets.

## Before adding content

Do not mass-produce abilities, items, quests, mobs, recipes, or map content until the owning system has:

1. a documented domain model,
2. stable identifiers,
3. validation rules,
4. serialization/load rules,
5. debug inspection,
6. automated tests or simulation where practical.

## Code rules

- The logical server owns gameplay truth.
- Client code presents state and submits intents; it does not authoritatively award damage, XP, loot, quest progress, currency, or reputation.
- Game-specific numbers belong in data unless they define an engine invariant.
- Cross-content references use stable namespaced IDs.
- Avoid one-off implementations when a reusable effect, condition, objective, or action primitive is appropriate.
- No general-purpose content scripting enters core production without an ADR.

## Documentation rules

Any architecture-changing implementation must update the relevant document under docs/ and, when it changes a durable decision, add or amend an ADR.

## Generated files

Do not hand-edit generated outputs. Change the source definition or generator and regenerate.
