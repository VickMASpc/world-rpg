# Quest doctrine

Status: ACCEPTED FOR GROUNDING — ORIGINAL LONG-FORM QUEST INTENT PRESERVED

World RPG quests are not a conveyor belt of short tasks.

They are long-lived reasons to move through the authored world.

A quest may:

- begin in one settlement;
- send the player across several regions;
- remain active for multiple sessions or real days;
- intersect with profession, reputation, class or dungeon goals;
- change meaning as the player learns more;
- end much later than it began.

The quest system must support short errands, but short errands are not the default shape of the game.

---

# Quest life

A quest exists inside the character's life.

The player may accept it and then:

- delay it;
- make partial progress;
- pursue another chain;
- discover a shortcut relevant to it;
- gain levels before returning;
- complete another profession/reputation objective along the route;
- decide an area is still too dangerous;
- return several sessions later.

This is healthy.

The journal exists partly because the player should not have to complete every intention immediately.

---

# Quest duration

There is no universal target duration.

Valid shapes include:

- a local errand;
- a one-session adventure;
- a chain lasting several hours;
- a long-running story that remains relevant across levels;
- a dungeon/class/profession objective requiring preparation and travel;
- a thread that pauses until the player becomes stronger.

The important rule is:

**duration follows the world/story/objective, not a standardized session box.**

A quest does not become bad because it survives several sessions.

---

# Quest chains

Long chains are important.

A chain should feel like growing involvement rather than repeated task generation.

Good progression can include:

- learning a local problem;
- investigating;
- travelling elsewhere;
- meeting another NPC/faction;
- discovering a larger cause;
- preparing for a difficult step;
- entering a dangerous place;
- returning later for consequences/follow-up.

The player should remember chains as stories tied to geography.

Avoid chains that are only:

accept
-> kill ten nearby enemies
-> return
-> accept next step
-> walk back to the same place
-> repeat.

---

# Quest starts

Quests do not all need conspicuous universal markers.

Possible starts include:

- NPC conversation;
- item found in the world;
- object interaction;
- entering/discovering a place;
- reputation state;
- class/profession milestone;
- previous quest consequence;
- rare enemy;
- dungeon event;
- rumor learned in a settlement.

The world should sometimes invite attention without displaying a task icon over every meaningful person.

Exact presentation conventions remain a UI decision.

---

# Journal doctrine

The journal is an important tool because quests can live for a long time.

It should preserve:

- original context;
- important dialogue/information;
- current objective;
- geographic clues;
- completed steps where useful;
- chain relationships when known.

The journal should help the player remember:

**why am I doing this?**

not merely:

**what is the current counter?**

A long-lived quest becomes frustrating if returning after three days means the player has no way to reconstruct its context.

---

# Objective families

The engine may support typed objectives such as:

- kill;
- collect;
- interact;
- talk;
- discover;
- escort;
- defend;
- use item/ability;
- dungeon/boss;
- composite conditions.

These are implementation primitives.

They are not the design of the quest.

"Kill 12 wolves" and "hunt a specific pack that has been attacking a road" may use the same primitive while producing very different experiences.

Content design must not be generated mechanically from primitive availability.

---

# Geographic directions

Quests should be able to describe the real world.

Directions may reference:

- roads;
- rivers;
- bridges;
- settlements;
- ruins;
- hills;
- caves;
- crossroads;
- named landmarks;
- cardinal/relative direction.

The default game should not depend on exact GPS markers.

The player should be capable of reading:

"follow the north road past the ruined watchtower, then leave it near the river bend"

and making that instruction part of their world knowledge.

The journal must preserve enough of this information to remain fair.

---

# Search areas

Approximate map support can exist where appropriate.

The doctrine is not "make every quest deliberately hard to find."

The doctrine is:

- guidance supports navigation;
- guidance does not replace geography.

A broad search region may be appropriate.

An exact glowing line to the final object should not be the universal default.

---

# Quest overlap

The player may carry several objectives into the same general region.

This is desirable when it emerges naturally.

It allows expedition planning:

- one story objective;
- profession gathering;
- a reputation task;
- a class objective;
- a rumor/detour.

However, the game should not turn the journal into a route-optimization spreadsheet where every trip exists to clear a cluster of markers.

Some quests should pull the player in different directions.

Some should remain unfinished.

---

# Failure and recovery

Quests may contain failure states where they make sense.

Examples:

- escort target dies;
- timed defense fails;
- ritual interrupted.

Failure should usually be recoverable.

The project should avoid casually destroying hours/days of long-term quest history because of one mechanical failure.

Restarting a local step may be appropriate.

Erasing a long chain is usually not.

---

# Branches and consequences

The engine should support:

- prerequisites;
- mutually exclusive branches;
- hidden/conditional starts;
- player-local consequences;
- shared-world consequences where technically appropriate;
- reputation/class/profession restrictions;
- world flags.

Not every quest needs a branch.

Choice should exist where the fiction/system benefits from it, not as mandatory dialogue decoration.

---

# Quest rewards

Quest reward doctrine follows `REWARD_DOCTRINE.md`.

Valid rewards include:

- money;
- reputation;
- equipment;
- recipes/materials;
- class/trainer access;
- transport/world access;
- information;
- follow-up chains;
- relationship/world changes.

A quest does not need an item reward to justify its existence.

---

# Return and turn-in

Physical return matters.

Reporting back to an NPC can:

- close the expedition;
- reconnect the player with town;
- deliver consequence/dialogue;
- create a new thread.

But return must make world/story sense.

Reject artificial structures where the player is repeatedly sent back and forth along the same short path only to stretch duration.

Long return travel is valid when the expedition genuinely took the player far away.

---

# Quest density

The world should not resemble a field of exclamation marks.

A settlement can contain:

- people with no quest;
- people whose quest appears later;
- merchants/trainers/profession NPCs;
- contextual dialogue;
- rumors;
- ambient life.

A player arriving somewhere should not immediately feel obligated to vacuum up every available task before leaving.

The journal should contain **intentions**, not a mandatory checklist.

---

# Long-lived relevance

Some quests can remain relevant after the player has outleveled their first steps.

Reasons include:

- story;
- reputation;
- class progression;
- profession reward;
- access;
- dungeon objective;
- unique item;
- later consequence.

This helps old regions remain part of the character's life.

---

# Quest anti-patterns

Reject:

- quest-hub conveyor design;
- mandatory immediate back-and-forth;
- every quest being a short kill/collect loop;
- dialogue that exists only to hide counters;
- exact GPS as the universal default;
- deleting long-term progress on small failures;
- chains fragmented only to inflate step count;
- quests written independently of geography;
- reward spam used to force completion;
- treating unfinished quests as failure.

---

# Quest playtest questions

For a short local quest:

- Did it belong to the place/person rather than feel randomly generated?

For a multi-session chain:

- Can the player return after several real days and understand the context from the journal?
- Did geography become part of the memory of the chain?

For an expedition quest:

- Did preparation/travel/resource state matter?
- Was physical return meaningful if required?

For the first province:

- Are there quests the player sees/starts but does not finish in the first session?
- Is there at least one thread that remains alive across multiple levels?
- Does the player remember a chain by place and people, not by objective counter?

If quests become a sequence of markers cleared as fast as possible, the doctrine failed.
