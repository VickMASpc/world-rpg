# P4 explicit power scaling

Status: PRODUCTION MECHANISM IMPLEMENTED — COEFFICIENT CALIBRATION DRAFT

Damage school no longer needs to answer the unrelated question:

Which offensive stat scales this effect?

New CombatMagnitudeRequest instances can carry explicit CombatPowerScaling terms.

Each CombatPowerTerm contains:

- a StatKey;
- a coefficient.

The resolver sums stat value times coefficient for every term.

This permits, for example:

- Physical damage that scales from Spell Power;
- Fire damage that scales from Attack Power;
- hybrid effects that consume both Attack Power and Spell Power;
- zero-power utility/flat effects.

School remains responsible for school semantics such as resistance. It does not define the power source for explicit requests.

## Compatibility mode

Existing fixtures still compile through CombatPowerScaling.LEGACY_PROFILE.

Only that explicitly named compatibility mode retains the old inference:

- Physical damage -> Attack Power and physical profile coefficient;
- other damage -> Spell Power and spell profile coefficient;
- healing -> Healing Power and healing profile coefficient.

New production content should not use legacy mode.

## First spell coefficient draft

WorldRpgCoefficientDraft provides an inspectable authoring helper:

- direct cast: cast duration / 3.5 seconds, capped at 100%;
- instant spell-like effect: 1.5-second equivalent = about 42.86%;
- channel: total duration follows the direct-cast rule, then the total coefficient can be divided across scheduled ticks.

These helpers produce authored coefficients. The production resolver does not inspect cast duration.

That separation is deliberate: special abilities are allowed to break the reference convention explicitly.

## Still open

- weapon-cycle / normalized weapon coefficient convention;
- rank-to-rank base magnitude growth;
- class-specific coefficient exceptions;
- AoE coefficient treatment;
- periodic coefficient treatment beyond fixed channels;
- final 1-100 ability budget tables.

The important architectural rule is already enforced: damage school is not the production power-scaling identity.