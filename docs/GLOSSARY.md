# Domain glossary

Definition: immutable authored game data with a stable namespaced ID.

Instance: mutable runtime state created from or referring to a definition.

ID: stable ResourceLocation-like identity. Display names, filenames and coordinates are not IDs.

Registry: indexed collection of validated definitions for one domain.

Effect: discrete gameplay operation such as damage, heal, interrupt, resource change or aura application.

Aura: persistent timed/conditional state attached to an entity.

Condition: typed predicate controlling legality or branching.

Cast: server-owned execution lifecycle from activation intent to resolution.

Proc: triggered secondary gameplay event with explicit rules.

Static state: authored definition data.

Runtime state: mutable play state.

Player state: runtime state owned by a character.

World state: shared save-owned runtime state.

Presentation hook: client-facing animation/VFX/audio/UI reference that never decides gameplay outcome.

Authoring source: human-edited source data/assets.

Generated output: deterministic machine-produced files.

Validator: tooling that rejects malformed, contradictory, missing or cyclic content.

Simulation: headless execution of formulas/content assumptions for balance and regressions.
