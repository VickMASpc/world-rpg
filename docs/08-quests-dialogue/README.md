# Quests and dialogue

Status: GROUNDED — CONTENT/COMPILER DETAILS STILL DRAFT

The governing quest intent lives in:

- `docs/00-constitution/QUEST_DOCTRINE.md`
- `docs/00-constitution/SETTLEMENT_DOCTRINE.md`

Quests are long-lived state machines built from typed objectives, conditions, actions and dialogue transitions rather than general-purpose scripts.

The engine must support:

- prerequisites;
- exclusive branches;
- hidden/conditional starts;
- multi-session chains;
- recoverable progress;
- player/shared-world consequences where appropriate;
- rewards/world flags/unlocks;
- class/profession/reputation restrictions.

Baseline objective primitives may include kill, collect, interact, talk, discover, escort, defend, use item/ability, dungeon/boss and composites.

Those primitives do not define content.

The quest design goal is long-lived intentions grounded in geography, people and consequences rather than a quest-hub conveyor.

Dialogue is a graph referencing stable NPC/quest/action/condition IDs. Presentation is client-side; authoritative transitions occur on the server.

The journal must preserve enough context and geographic direction for a player to resume a chain after real days away without universal exact GPS guidance.
