# Quests and dialogue

Status: DRAFT

Quests are long-lived state machines built from typed objectives, conditions, actions and dialogue transitions rather than general-purpose scripts.

Baseline objective families: kill, collect, interact, talk, discover, escort, defend, use item/ability, dungeon/boss and composite objectives.

The engine must support prerequisites, exclusive branches, hidden/conditional starts, multi-session chains, player versus shared-world consequences, recoverable progress, rewards, world flags, unlocks and class/profession restrictions.

Dialogue is a graph referencing stable NPC/quest/action/condition IDs. Presentation is client-side; authoritative transitions occur on the server.

Quest text should support real geographic directions without requiring exact GPS markers.
