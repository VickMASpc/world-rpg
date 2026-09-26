# Anti-pattern catalog

Status: ACCEPTED FOR GROUNDING — STOP-WORK AUTHORITY

This document defines patterns that can look like progress while moving World RPG away from its intended identity.

It exists to make rejection easier.

A feature may be technically sound, familiar from successful games, convenient for players, easy to implement in Minecraft, easy to test, or attractive in isolation and still be wrong for this project.

When an anti-pattern is detected, the default response is not "tune it later."

The default response is:

1. stop expanding the feature;
2. identify which constitutional invariant is being damaged;
3. find a design that preserves the intended experience;
4. require explicit amendment if preservation is impossible.

The governing rule remains:

**Preserve the idea unless there is a demonstrated reason to change it.**

---

# 1. False slowness

## Symptom

The game consumes time without producing memory, decisions, tension, attachment, preparation, discovery, or meaningful return.

Examples:

- mandatory repeated walks over the same route with nothing new learned or decided;
- arbitrary collection counts inflated only to lengthen a quest;
- long interaction animations repeated constantly without gameplay purpose;
- low drop rates used as the only reason an objective takes an hour;
- repair, resupply, or inventory friction so frequent that they become chores rather than expedition rhythm;
- enemies with inflated health but no additional tactical texture;
- forcing the player to wait because the target hour count looks too low.

## Why it is dangerous

World RPG deliberately consumes a lot of time. That makes padding especially dangerous because raw duration can masquerade as success.

## Reject when

Removing the time cost leaves the same memories, decisions, world knowledge and risk profile.

---

# 2. Convenience creep

## Symptom

Small quality-of-life additions accumulate until distance, preparation and civilization no longer matter.

Examples:

- remote bank access;
- universal portable repair;
- trainers through a menu;
- instant access to every profession station;
- unrestricted destination teleportation;
- automatic quest routing that removes navigation;
- infinite or effectively infinite field inventory;
- remote auction/market access if it erases settlement relationships;
- automatic resupply that makes expedition preparation irrelevant.

## Why it is dangerous

No single convenience needs to destroy the game. The danger is cumulative.

A world built around travel can be silently converted into a menu through dozens of individually defensible shortcuts.

## Review question

**What reason to know, visit, prepare for, or return to a place disappears if this feature exists?**

If the answer is substantial, the convenience requires explicit justification.

---

# 3. World-as-menu design

## Symptom

Geography becomes visual decoration around destination selection.

Examples:

- the first journey to important places is skipped;
- discovered travel networks collapse almost all future movement;
- regions become isolated level-select screens;
- quests routinely teleport players to their objective;
- roads, passes, rivers, bridges and terrain do not alter choices;
- players know map icons but cannot describe how places connect.

## Failure test

Ask a player to sketch the first province from memory.

If they remember icons and names but not routes, barriers, shortcuts, settlement relationships or danger transitions, geography is failing.

---

# 4. Disposable-zone progression

## Symptom

A region is useful only for a narrow level band and becomes dead immediately afterward.

Examples:

- all quests in a region complete at once and never send the player back;
- every vendor/trainer/service is duplicated in the next zone;
- profession materials have no later relevance;
- transport routes do not make old settlements strategically useful;
- old dungeons, factions, NPCs and reputation cease to matter as soon as XP efficiency drops.

## Why it is dangerous

The project wants a character with history, not a tourist moving through disposable theme parks.

Old regions may become easier and less central. They should not be systematically erased from the character's life.

---

# 5. Gear treadmill

## Symptom

Items are replaced so quickly that the player cannot remember them.

Examples:

- upgrades every few minutes;
- a new quest reward invalidates the previous quest reward immediately;
- item level/power inflation overwhelms unusual traits;
- equipment is treated as a temporary number packet rather than character history;
- rare or difficult acquisitions remain useful for only a tiny fraction of the time spent obtaining them.

## Guardrail

A strong item remaining useful for roughly 10–20+ hours is a valid and desirable outcome when appropriate.

That is not a universal hard duration; it is a reminder that longevity is allowed.

---

# 6. Reward confetti

## Symptom

The game pays the player constantly because designers are afraid that travel, story, knowledge or progression will feel insufficient on their own.

Examples:

- a chest at every short objective;
- gear rewards for nearly every quest;
- constant rarity escalation;
- XP, currency, reputation and loot all exploding together for routine actions;
- meaningless achievements/unlocks added to every interaction.

## Why it is dangerous

Constant reward noise destroys contrast and accelerates history churn.

A quest can be valuable because it reveals information, opens access, advances a relationship, improves reputation, teaches a route, enables a profession, or concludes a remembered story.

---

# 7. Quest-hub conveyor

## Symptom

The player arrives, collects a cluster of tasks, clears nearby map markers, returns, and moves to the next hub.

Examples:

- every NPC with meaningful dialogue is marked as a task dispenser;
- objectives are spatially optimized into efficient loops regardless of fiction;
- chains are fragmented into many short turn-ins to manufacture progression beats;
- the journal is primarily a route-optimization checklist;
- unfinished quests are treated as inefficiency;
- a new settlement feels like a new checklist rather than a new place.

## Why it is dangerous

This is one of the easiest ways to build a technically complete RPG that directly contradicts the original idea.

---

# 8. GPS replacing navigation

## Symptom

Quest guidance becomes so exact that the player never needs to read geography.

Examples:

- universal glowing paths;
- exact objective coordinates for everything;
- permanent arrows that remove route decisions;
- objectives written without usable directions because UI markers are assumed.

## Important distinction

The opposite extreme is also wrong.

Navigation should not become an exercise in hiding information from the player.

Journal context, named landmarks, approximate search areas and map support can all be fair.

The goal is to let guidance support world knowledge rather than replace it.

---

# 9. Civilization bypass

## Symptom

Players can remain indefinitely in the field without meaningful reasons to return.

Examples:

- full repairs, storage, training and crafting anywhere;
- no meaningful economic decisions;
- no long-lived NPC or service relationships;
- settlements exist mainly as quest scenery;
- inns have no systemic role;
- transport is detached from physical places.

## Failure test

After a three-hour expedition, ask:

**Why am I glad to be back?**

If the answer is only "to turn in quests," civilization is too weak.

---

# 10. Combat as permanent emergency

## Symptom

Every ordinary fight demands maximum attention, input rate, cooldown tracking and reaction speed.

Examples:

- constant proc lights and reactive buttons;
- every enemy requiring several interrupts/mechanics;
- no calm pulls;
- resources resetting after each encounter because attrition is considered inconvenient;
- every ability being instant because cast commitment feels slow;
- visual/audio noise used to make routine combat feel exciting.

## Why it is dangerous

Difficult pulls are memorable because routine combat breathes.

The project needs room for deliberate, readable combat and sustained travel/exploration sessions, not a permanent encounter simulator.

---

# 11. Combat as health-sponge waiting

## Symptom

Combat is technically slow but decisions are finished long before the enemy dies.

Examples:

- huge health pools with unchanged enemy behavior;
- long rotations with no resource, position, interrupt, target or recovery decision;
- slow attack cadence that merely extends animation time;
- low damage used as the main lever for "old-school" pacing.

## Failure test

If the player can determine the result early and then simply repeat the same safe action until the bar empties, slowness is not creating depth.

---

# 12. Global scaling

## Symptom

The world follows the player closely enough that geography loses objective danger and progression loses contrast.

Examples:

- low-level enemies continuously scaling to remain equal threats;
- region difficulty driven primarily by player level rather than authored world position;
- old roads and enemies never becoming meaningfully easier;
- high-level areas being made safe because the player reached them early.

## Why it is dangerous

A fixed world lets the player remember fear, growth, avoidance and return.

The project explicitly rejects global level scaling as the default world model.

---

# 13. Explosive power inflation

## Symptom

A small level gap or short gear interval turns remembered threats into instant deletion.

Examples:

- +5 levels erasing ordinary enemies;
- gear tiers multiplying output so aggressively that the previous tier becomes irrelevant;
- stat growth that forces constant replacement just to keep formulas stable.

## Desired contrast

Progress should be real but gradual.

The current grounding target allows +5 to feel easier, +10 clearly easier, +20 to trivialize many ordinary old threats, and +30+ to make routine old enemies genuinely trivial.

Those are experiential anchors, not exact formulas.

---

# 14. Endgame gravity

## Symptom

Systems treat levels 1–99 as preparation for "the real game."

Examples:

- early professions intentionally disposable;
- class identity postponed until near cap;
- meaningful dungeons/reputation/economy reserved for endgame;
- leveling content produced as filler because cap content receives all systemic depth;
- progression accelerated merely to move players toward level 100.

## Constitutional conflict

**Leveling is the game.**

The 1–100 journey must contain the game's identity, not preview it.

---

# 15. Checklist progression

## Symptom

Progress is measured by how many systems, bars or collections were completed rather than by the character's accumulated life.

Examples:

- mandatory completion percentages;
- every region having an identical checklist;
- exploration reduced to collectible cleanup;
- professions treated as another linear bar detached from economy/world;
- reputation existing only as reward tiers with no faction relationship.

Progression should create new capabilities, relationships, routes, knowledge and history—not simply more filled meters.

---

# 16. Architecture as product

## Symptom

The repository grows sophisticated while the player's life remains vague.

Examples:

- adding abstractions because they make the architecture elegant;
- creating more simulator scenarios because they are measurable;
- building generalized systems before a real content need exists;
- interpreting clean tests as proof of good game design;
- measuring progress by package count, LOC, registry breadth or API coverage.

## Stop rule

Before a new abstraction or feature family is admitted, name the grounded player experience it protects or enables.

If that cannot be done, it is not currently justified.

---

# 17. Synthetic fixture becoming doctrine

## Symptom

A test number or headless scenario silently becomes a game-design commitment.

Examples:

- a level-20 simulator health value becoming the production health curve;
- proof abilities becoming class abilities;
- a dungeon stress fixture defining party composition;
- a test cooldown becoming the universal GCD;
- a mock enemy behavior becoming the world AI standard.

Fixtures prove mechanisms and relationships.

They do not gain design authority through repeated use.

---

# 18. Feature-family momentum

## Symptom

Once a system exists, adjacent features are added because they are obvious next engineering steps.

Examples:

- stats leading automatically to every familiar RPG rating;
- threat leading automatically to full MMO tank doctrine;
- an aura system leading automatically to dozens of proc types;
- networking support leading automatically to multiplayer-first design;
- one fast-travel node leading automatically to comprehensive teleport UI.

The question is not "what usually comes next?"

The question is "what does this game need next?"

---

# 19. Content inflation

## Symptom

Scale targets are treated as quotas.

Examples:

- thousands of items produced before item identity/longevity is proven;
- hundreds of quests generated from templates to fill the map;
- land expanded because the continent target is large even though route structure is weak;
- enemy variants created through stat/name swaps without ecological or gameplay purpose;
- particle/model volume used as evidence of richness.

The project may eventually contain enormous content volume.

Volume follows a working content plant and proven doctrine. It does not substitute for them.

---

# 20. Procedural sameness disguised as scale

## Symptom

Generation makes many things that are technically different but experientially interchangeable.

Examples:

- item affix permutations with no memorable role;
- quests assembled from random objective templates;
- settlements generated from identical service layouts;
- enemy families differing only by health/damage multipliers;
- roads/terrain with no authored navigation purpose.

Generation is useful for production, validation and controlled variation.

Authored structure remains responsible for memory.

---

# 21. Minecraft leakage

## Symptom

Minecraft defaults quietly determine World RPG's design because overriding them is inconvenient.

Examples:

- vanilla inventory size deciding expedition economy without review;
- vanilla hunger becoming the recovery model by default;
- vanilla mob spawning defining encounter ecology;
- vanilla death/respawn defining RPG death consequences without doctrine;
- vanilla dimensions/teleports defining travel;
- vanilla combat timing surviving only because replacement work is difficult.

Minecraft is the runtime substrate, not the design authority.

When a default happens to fit, keep it deliberately.

When it does not, adapt or replace it.

---

# 22. Modernity bias

## Symptom

A feature is considered automatically better because contemporary games commonly provide it.

Examples:

- removing all travel friction;
- auto-loot for every context;
- universal objective tracking;
- matchmaking/teleport assumptions;
- immediate free respecs;
- centralized menus replacing world services.

Modern convenience is neither inherently good nor inherently bad.

It must pass the same lived-experience test as every other design.

---

# 23. Nostalgia bias

## Symptom

An inconvenient system is protected merely because an older RPG/MMO used something similar.

Examples:

- arbitrary corpse runs;
- obscure information with no fair in-world source;
- excessive inventory shuffling;
- punishing failure with hours of repeated content;
- deliberately awkward UI;
- low drop-rate grinding with no world meaning.

The project is not reenacting old inconvenience.

It is preserving the kinds of commitment, place, history and rhythm that older designs sometimes created.

---

# 24. Every minute must be exciting

## Symptom

Quiet travel, routine combat, settlement organization and uneventful road time are treated as design failures.

## Why it is dangerous

The original idea needs contrast.

A dangerous ruin feels dangerous partly because the road before it was calm.

A town feels safe partly because the remote expedition was tiring.

A rare item feels meaningful partly because rewards were not constant.

Quiet is allowed.

Empty is not.

---

# 25. Every minute must be productive

## Symptom

The game rewards or progresses the player continuously so no action can be "wasted."

Examples:

- every path contains gathering nodes;
- every NPC produces a task;
- every trip is optimized to stack objectives;
- every fight must grant meaningful advancement;
- exploration without rewards is considered invalid.

Some time can exist for orientation, curiosity, anticipation, scenery and social/world texture.

That time still needs to contribute to the player's relationship with the world.

---

# 26. Forced efficiency

## Symptom

Content assumes players must optimize routes, quest stacks, builds and downtime to avoid falling behind expected pacing.

World RPG should support mastery and planning, but ordinary play must not require treating the game as a spreadsheet.

A player should be able to:

- wander;
- keep an inefficient favorite item;
- postpone a quest;
- return to a familiar town;
- explore a rumor;
- spend a session mostly travelling/professing/socializing with the world

without being structurally punished for not maximizing XP/hour.

---

# 27. The first province as tutorial corridor

## Symptom

The opening hours rapidly demonstrate every system, give constant unlocks, and push the player forward before a home region can become familiar.

Examples:

- immediate parade of all six class mechanics;
- every profession introduced in the first hour;
- multiple settlements reached too quickly;
- rapid gear replacement intended to teach rarity colors;
- no dangerous visible destination the player must postpone;
- no unfinished quest or place surviving beyond a session.

The opening must teach through living in the world, not through a feature tour.

---

# 28. Physical proof theater

## Symptom

A Minecraft test room is declared successful because the mod loads and a button causes an effect.

The P3 gate requires more:

- real entity-backed actor state;
- client intent/server authority;
- cast commitment;
- movement/range/LOS revalidation;
- persistent resources;
- cooldown authority;
- shared aura/item stat participation;
- clean lifecycle/reset behavior;
- structured failure evidence.

Do not reduce the matrix because manual testing is inconvenient.

---

# 29. Progress theater

## Symptom

Repository activity is mistaken for game progress.

Warning signs:

- many commits but no stronger playtest claim;
- many checked boxes but doctrine remains vague;
- issue/PR completion becomes the goal;
- implementing the next obvious subsystem without revisiting the original idea;
- changing expectations after a test fails so the test can become green.

The correct question is always:

**What can the player now experience—or what can we now prove about that future experience—that we could not before?**

---

# 30. Conservative stop-work checklist

Before expanding a major feature, ask:

1. Does it preserve geography?
2. Does it preserve reasons to prepare and return?
3. Does it preserve slow, readable accumulation of character history?
4. Does it create memory or only duration?
5. Does it create decisions or only friction?
6. Does it preserve calm as well as danger?
7. Does it make old places/items/NPCs disposable too quickly?
8. Does it add convenience that removes a relationship with the world?
9. Is a test fixture being mistaken for doctrine?
10. Are we building it because the game needs it, or because the architecture makes it easy?
11. Can we describe the observable player evidence that would prove it works?
12. If it failed that evidence, would we change the feature rather than weaken the test?

A serious "yes" to an anti-pattern is sufficient reason to stop expansion.

---

# Final rule

World RPG is allowed to be slow, large, inconvenient in selected ways, quiet, demanding, old-fashioned in selected ways, and resistant to optimization.

It is not allowed to be slow merely because we want a large hour count.

It is not allowed to be inconvenient merely because inconvenience existed in older games.

It is not allowed to be huge merely because the project has huge targets.

The desired result is a character who accumulates places, routes, gear, people, obligations, skills and stories over hundreds of hours.

Any system that makes that accumulated history less important must justify itself before implementation continues.
