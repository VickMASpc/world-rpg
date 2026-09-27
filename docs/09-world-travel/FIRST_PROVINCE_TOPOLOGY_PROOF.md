# First province topology proof

Status: ACCEPTED FOR PRE-TERRAIN VALIDATION — GEOMETRY AND MINUTES PROVISIONAL

This is a route/topology prototype, not production terrain.

Its purpose is to prove that the first province can create:

- local familiarity;
- meaningful settlement separation;
- route choice;
- danger pockets;
- remote support;
- first-time journeys long enough to matter;
- learned shortcuts;
- reasons to return.

Do not build final terrain art from this document directly.

---

# Node roles

Use the same placeholder roles as `FIRST_15_HOURS_STORYBOARD.md`:

- A — home settlement;
- B — local wild/work land;
- C — main road corridor;
- D — river crossing / bridge / route fork;
- E — visible high-level danger pocket;
- F — remote refuge / outpost / inn;
- G — regional settlement;
- H — deeper outward route beyond the opening chapter.

Additional optional nodes:

- B2 — profession-rich side area;
- C2 — alternate path or forest track;
- D2 — ford/shortcut unlocked through knowledge;
- R1 — local ruin/cave objective;
- R2 — difficult unfinished site;
- T1 — future transport node physically located at G or F.

---

# Abstract route graph

```text
                 E (danger pocket)
                / \
               /   \
          C2 --     -- R2
         /             \
A -- B -- C ---- D ----- F ---- H
 \       |      |       \
  \     R1     D2 ------- G
   \
    B2
```

This is not a literal map shape.

It encodes relationships:

- A connects quickly to B;
- C is the reliable outward spine;
- D is a memorable navigation decision point;
- E is visible/approachable but dangerous early;
- F extends safe expedition reach;
- G is a true regional destination, not adjacent to A;
- H demonstrates continuation beyond the first chapter;
- C2/D2 allow knowledge-based route improvement;
- R1/R2 create local and unfinished dangerous destinations.

---

# Provisional travel-time bands

These are **measurement targets for a graybox**, not doctrine-level constants.

They exist so we can detect a topology that is obviously too compressed or artificially stretched.

Assume ordinary on-foot travel without combat, gathering, detours or getting lost.

| Route type | Prototype target |
| --- | ---: |
| A -> nearest local wild edge B | ~2–5 min |
| A -> local objective/ruin R1 | ~6–12 min |
| A -> major fork/landmark D | ~10–18 min |
| D -> refuge F | ~8–15 min |
| A -> refuge F by safe route | ~20–30 min |
| A -> regional settlement G, first journey | ~25–40 min |
| G -> A using learned shortcut later | meaningfully shorter, but still a journey |
| F/G -> deeper direction H threshold | ~15–30 min before deeper content |
| full outbound objective + fighting/gathering + return | commonly 1–3 h |

These values must be revised from actual play, not defended because they appear in a table.

A quiet twenty-minute trip can be correct.

A forty-minute trip can also be correct.

The question is whether the geography remains legible and the journey creates anticipation, decisions, memory or expedition weight.

---

# Route-choice requirements

The topology must permit at least three meaningful travel decisions.

## Safe road versus risky shortcut

The safe path should be:

- easy to follow;
- reliable;
- longer or less resource-rich.

The shortcut may be:

- shorter;
- more dangerous;
- harder to navigate;
- gated by knowledge rather than an arbitrary menu unlock.

## Detour versus expedition goal

A profession/resource-rich side area should tempt the player away from the efficient route.

The choice should consume:

- time;
- bag space;
- resources;
- risk budget.

## Push onward versus return

Near F / remote territory, the player should have enough accumulated distance that continuing toward H or R2 feels materially different from turning back.

This choice is impossible if support is never far away.

---

# Dangerous-place requirement

E must be visible or naturally discoverable during early traversal.

It must not be placed behind a hard gate solely to protect the player.

The graybox should allow the player to:

- approach;
- observe danger;
- retreat;
- remember the location;
- return much later stronger.

The final content may communicate danger through levels, silhouettes, NPC warnings, environment, enemy behavior or a combination.

---

# Settlement hierarchy requirement

A and G must not be identical service bundles.

The topology proof should record why a player might use each:

## A — home

- familiarity;
- early relationships;
- local route access;
- selected services.

## F — refuge

- limited safety/support;
- extends expedition reach;
- intentionally incomplete service set.

## G — regional settlement

- broader infrastructure;
- new outward routes;
- service/faction/profession identity not available everywhere.

If G simply replaces A, the topology has failed settlement continuity.

---

# Mental-map landmarks

The graybox needs at least five visually or structurally distinct navigation anchors even before final art.

Examples of roles, not final assets:

- bridge/river bend;
- tower/ridge silhouette;
- fork with sign/stone marker;
- steep forest edge;
- outpost roof/fire/light visible from distance;
- road entering a valley;
- ruined wall beside the route.

The test is whether the player can recover orientation after intentionally leaving the road.

---

# Quiet-space requirement

Do not populate the whole graph with encounters just to validate it.

At least one representative road leg should contain several uninterrupted minutes where navigation, ambience and distant landmarks carry the experience.

During graybox testing, record whether the problem is:

- route too long;
- route visually illegible;
- route spatially repetitive;
- destination not anticipated;
- no meaningful decision;

before concluding that the answer is to shorten it or add enemies.

---

# Cheap graybox implementation

Before production terrain, build a disposable Minecraft test world or generated flat-ish route layout using cheap blocks/markers.

Recommended representation:

- colored wool/concrete only for route classes and node labels;
- primitive hills/walls to block sight lines;
- simple river trench / bridge;
- crude towers/columns as landmarks;
- signs with node IDs;
- no decorative architecture requirement;
- no custom mobs required for travel timing;
- optional hostile vanilla placeholders only where danger affects route choice.

The purpose is measurement, not screenshots.

---

# Measurement sheet

For each route run, record:

```text
run id:
route:
first-time or known:
start/end:
walking time:
time spent checking map:
wrong turns:
shortcut used:
combat/gathering excluded or included:
landmarks remembered afterward:
subjective state: local / journey / remote:
notes:
```

Run at least:

1. A -> R1 local objective;
2. A -> G safe first journey;
3. G -> A after learning shortcut;
4. A -> F -> remote side objective -> F/A;
5. D -> E cautious approach/retreat;
6. one 60–180 minute mixed expedition including combat/gathering once those placeholders exist.

---

# Success criteria

The topology is ready for more expensive world work when:

- local destinations feel near without being instant;
- A and G feel distinctly separated;
- first A -> G travel is memorable enough that repeat travel has context;
- a shortcut produces a genuine "I know a better way" feeling;
- the player can identify safer versus riskier routes;
- F genuinely extends expedition reach;
- E creates early remembered danger;
- quiet road time is legible rather than featureless;
- the player can verbally reconstruct the graph after several runs;
- at least one reason exists to return to A after reaching G.

---

# Failure modes

Reject or revise the topology if:

- A -> G feels like walking across a large town rather than traveling;
- every useful route is a straight line;
- shortcuts save trivial time or remove all travel;
- danger is uniformly smeared instead of geographically learned;
- F is redundant because A/G are always close;
- landmarks cannot reorient the player;
- repeated routes remain indistinguishable after several trips;
- distance exists only because paths were stretched with no geographic decisions;
- the optimal path is so dominant that route choice is fake.

---

# Relationship to physical P3 evidence

This topology proof and the P3 combat-room evidence answer different questions.

The topology asks:

**Can the opening world produce the intended geography and travel rhythm?**

P3 physical evidence asks:

**Does the RPG kernel behave authoritatively against real Minecraft entities and world facts?**

Do not delay the P3 proof waiting for final terrain.

The P3 room is deliberately small and mechanical.

Do not treat P3 room success as proof that the province travel design works.
