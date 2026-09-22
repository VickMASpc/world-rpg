# P4 mathematical constitution

Status: ACTIVE — FORMULAS NOT YET FROZEN

P4 is the first phase allowed to define production combat/progression
mathematics.

## Ownership

P3 owns mechanism and authority.

P4 owns:

- numerical combat resolution,
- baseline progression curves,
- deterministic stochastic outcomes,
- simulator parity,
- calibration target bands.

A P4 formula must be callable without Minecraft.

## School vocabulary

Baseline schools:

- Physical
- Fire
- Frost
- Arcane
- Nature
- Holy
- Shadow

They are stable namespaced keys, not a Java-only closed identity model.

## Randomness

Combat formula code may not use global, thread-local, Minecraft or JDK ambient
randomness.

Every stochastic calculation receives an explicit CombatRollSource.

The baseline seeded implementation is World RPG's own SplitMix64 stream.

Given the same:

- content snapshot,
- actor state,
- event/input sequence,
- seed,

headless simulation and runtime formula code must consume the same ordered roll
stream and produce the same outcome.

Changing roll consumption order is therefore a simulation-visible behavior
change and should be covered by regression tests.

## Numerical discipline

Until a formula explicitly states otherwise:

- use finite double precision internally,
- do not round intermediate values for presentation,
- reject NaN and infinity at boundaries,
- authored base magnitude is an input, never the final outcome by definition,
- clamps must be explicit in the owning formula,
- unit conversions must be documented.

Final damage/healing rounding is **not frozen yet**.

## Scaling doctrine

Production formulas should prefer readable, slowly changing curves over hidden
exponential inflation.

The intended game is long and spatially slow. It should not require enormous
stat-number growth merely to make higher levels relevant.

Level difference, gear, talents and encounter mechanics may matter strongly
without making old numbers meaningless by orders of magnitude.

## Simulator doctrine

The simulator is not a separate approximation.

It must call the same production formula functions/resolver used by runtime
combat.

Scenario drivers may be simplified, but numerical resolution may not be
duplicated.
