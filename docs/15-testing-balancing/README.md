# Testing and balancing

Status: DRAFT

Layers:

1. formula/pure primitive unit tests,
2. content validation,
3. cross-registry integrity,
4. persistence round-trip and migration,
5. network-authority tests where practical,
6. headless balance simulation,
7. in-game developer arenas,
8. vertical-slice playtesting,
9. long-session development telemetry.

The simulator should consume the same or equivalent definitions/formulas as runtime code and report damage/healing, TTK, resources, downtime, threat, XP/hour, wealth/hour, loot distributions, gear replacement interval, profession rates and travel/quest estimates.

Critical target bands should have regression tests so a formula edit cannot silently double TTK or halve progression speed.
