# Combat mathematics bible

Status: ACTIVE DRAFT — P4 FEATURE EXPANSION PAUSED BY P4-G

Combat mathematics exists to support the lived RPG, not to become a checklist of every mechanic familiar from an MMO.

The project already has a substantial production resolver/simulator foundation:

- explicit resolution profiles and deterministic roll source;
- damage/healing magnitude requests;
- hit/miss and crit contracts;
- offensive scaling;
- physical armor and typed resistance mitigation;
- incoming/outgoing modifier hooks;
- absorb boundary;
- defeat events;
- level-aware derived/rating/reference curve machinery;
- headless simulation sharing the production cast/effect/resolution path.

Many documents in this directory are deliberately labeled draft, calibration, boundary or stress fixture.

They are not automatically final gameplay doctrine.

## P4-G freeze

Before adding more combat feature families, follow:

- `docs/00-constitution/P4_G_RECONVERGENCE_GATE.md`
- `docs/00-constitution/ANTI_PATTERN_CATALOG.md`
- `docs/STATUS.md`

Current priority is physical evidence:

1. record the P3 entity-backed Minecraft gate;
2. validate first-province travel/topology physically;
3. only then resume math work that is demanded by grounded player experience.

Do **not** interpret an old list of familiar RPG mechanics as a mandate to implement all of them.

Threat, taunt, dodge/parry/block, pets, group scaling, school locks, boss rules or any other mechanic family require an identified gameplay need before implementation expansion.

## Mathematical standard

When a formula is admitted, it should define:

- ownership and place in resolution order;
- units;
- clamps/domains;
- deterministic randomness inputs if any;
- authoritative precision/rounding behavior;
- worked examples where useful;
- simulator/runtime parity expectations.

The same production equations must remain usable by deterministic headless simulation so tuning and regressions can be tested outside Minecraft.

## Interpretation rule

Synthetic routine/elite/healer/dungeon scenarios are mechanical stress and calibration evidence.

They may reveal useful constraints, but they do not silently define final classes, enemy packages, party roles or encounter pacing.
