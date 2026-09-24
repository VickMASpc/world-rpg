# Grounding gate

Status: ACTIVE

## Why this gate exists

The architecture work began correctly, but the project started moving too quickly from "what experience are we building?" toward "what code infrastructure can we implement?"

For World RPG, that is dangerous.

A technically excellent framework can still produce a sterile RPG.

Therefore implementation work after the existing P2 transaction foundation pauses until this grounding gate is sufficiently developed.

## Rule for all future systems

Before a major system is designed or implemented, its specification must answer four questions:

1. **What player experience does this system exist to create?**
2. **What moment-to-moment or long-term rhythm does it support?**
3. **How could a technically correct implementation accidentally damage the spirit of the game?**
4. **What observable playtest result proves the system is helping?**

A system that cannot answer those questions is not ready for implementation.

## Grounding package

Before continuing deep P2 implementation, establish:

- [x] Spirit of the game.
- [ ] Player life-cycle: what it feels like to be level 1, 10, 25, 50, 75 and 100.
- [ ] Session rhythm: 30 minutes, 1 hour, 3 hours and a full weekend.
- [ ] World rhythm: safe -> wild -> dangerous -> remote -> civilization.
- [ ] Travel doctrine: first journey, repeated journey, route knowledge and transport progression.
- [ ] Combat doctrine: relaxed pull, difficult pull, elite, dungeon and boss tempo.
- [ ] Progression doctrine: what must change over 5, 10, 25 and 50 levels.
- [ ] Reward doctrine: why XP, items, money, abilities and discoveries feel valuable.
- [ ] Quest doctrine: what distinguishes an adventure from a task.
- [ ] Settlement doctrine: why town/inn/village visits matter emotionally and mechanically.
- [ ] Anti-pattern catalog: designs that look efficient but destroy the intended feel.
- [ ] First 15-hour experiential storyboard.

## Architecture is subordinate

The plant exists to support this.

Examples:

A content registry is useful because the game needs hundreds of long-lived authored places, enemies, abilities and quests without collapsing under maintenance.

A simulator is useful because slow progression must remain slow for the right reasons, not because accidental HP inflation made enemies tedious.

A quest engine is useful because chains should survive over several sessions and react to a persistent world.

A travel system is useful because geography needs to become memory.

A persistence system is useful because a player may spend hundreds of hours with one character and the world must remember that life reliably.

If a technical subsystem cannot be connected back to an experiential need, its priority is questionable.

## Exit condition

This is not a gate where every number must be final.

It exits when we can describe the intended player life with enough clarity that an engineer can reject a technically sensible feature because it would make the game feel wrong.

At that point P2 implementation resumes with the grounding documents open beside it.
