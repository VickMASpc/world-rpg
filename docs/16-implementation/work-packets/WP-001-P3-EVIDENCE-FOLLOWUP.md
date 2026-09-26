# WP-001 — P3 physical evidence follow-up

ID:
WP-001

TITLE:
P3 Physical Evidence Follow-up

STATUS:
COMPLETE

INTEGRATION_BRANCH:
`p4/math-simulator`

EXPECTED_BASE_SHA:
`45c5d2030920fc22c5dc9238de90c1d28830cddf`

WORK_BRANCH:
`codex/wp-001-p3-evidence-followup`

## OBJECTIVE

Continue the physical P3 evidence effort from the recorded Minecraft testing and
determine whether any remaining P3-G1 through P3-G18 row requires a real harness or
runtime correction before further manual testing. This packet does not authorize P3
or P4 expansion.

## WHY THIS ADVANCES THE RPG

The packet improves confidence that the existing RPG kernel behaves authoritatively
against real Minecraft entities and world facts. It advances the production plant's
physical proof; it does not add authored, playable, or presentable RPG content.

## AUTHORITATIVE DOCUMENTS

- `docs/16-implementation/ANCHOR_STATE.md`
- `docs/STATUS.md`
- `docs/00-constitution/SPIRIT_OF_THE_GAME.md`
- `docs/00-constitution/IDEA_FIDELITY_PACT.md`
- `docs/00-constitution/ANTI_PATTERN_CATALOG.md`
- `docs/00-constitution/P4_G_RECONVERGENCE_GATE.md`
- `docs/16-implementation/P3_GATE_VERIFICATION.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_RUNBOOK.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_2026-09-22.md`
- `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`

## IN SCOPE

- inspect the current matrix, evidence record, runbook, fixture abilities, Minecraft
  target observation, cast lifecycle, developer-room commands, and relevant tests;
- determine which remaining rows are already executable exactly as documented;
- identify any impossible, ambiguous, or misleading row setup;
- identify any runtime/kernel defect actually supported by current evidence;
- add only developer harness corrections or G-row-specific observability needed to
  make a row valid and repeatable;
- fix kernel, integration, or networking defects only when proven by evidence;
- add adjacent tests for changed behavior;
- correct evidence or runbook text where repository reality requires it;
- provide the minimal next physical Minecraft test batch.

## OUT OF SCOPE

- new P4 combat feature families;
- new gameplay mechanics;
- balance tuning or treating proof values as production balance;
- final class design or mass content;
- unrelated refactors;
- weakening any gate expectation;
- changing gameplay doctrine;
- beginning Era 3 or WP-002.

## INVARIANTS

- Bolt's authored proof-fixture maximum range is 12 blocks.
- The earlier accepted Bolt after a 20-block spawn is ambiguous because source
  position at activation was not recorded; it is not proof of a range defect.
- The controlled G9 test keeps the player stationary and uses the server-direct
  `room cast bolt` path, whose output must explicitly identify network bypass.
- A rejected activation does not mutate mana, target health, or cast state.
- Manual Minecraft PASS is recorded only from physically observed evidence.
- Proof fixture values and synthetic scenarios remain non-production doctrine.

## KNOWN EVIDENCE

- P3-G2 PASS: actual client/network activation path.
- P3-G4 PASS: committed timed Bolt applies its effect only at completion.
- P3-G10 PARTIAL (strong): three scheduled channel ticks observed from a non-fresh
  target baseline.
- P3-G1, G3, G6, and G12 have useful partial evidence.
- P3-G9 is unresolved because the earlier 20-block experiment omitted source
  position at activation.
- P3-G17 is pending because the earlier probe targeted the player, not the Husk.
- P3-G15 is an automated pass and P3-G18 passed for the previously tested candidate.
- Earlier freeform tests with ordinary moving entities are exploratory only and must
  not be conflated with controlled G9 evidence.

## REQUIRED WORK

1. Audit each remaining manual row against the current harness and implementation.
2. Classify every row as executable as documented, ambiguous/misleading, or blocked
   by a concrete defect.
3. Trace observed behavior through target observation, cast lifecycle, developer
   commands, and fixture definitions.
4. Implement and test only evidence-derived corrections that are necessary.
5. Reconcile the runbook/matrix when repository reality differs from documentation.
6. Produce a durable handoff with the smallest useful next manual test batch.

## TESTS / EVIDENCE REQUIRED

- For any code change, run the narrow relevant tests first.
- Run the complete normal project test/build suite before handoff.
- Record exact commands and results.
- Preserve the existing physical evidence statuses unless new physical Minecraft
  output is supplied; do not claim a new manual PASS from static inspection or tests.

## STOP CONDITIONS

- Stop and mark BLOCKED if a remaining row requires a product, gameplay, or scope
  decision belonging to the Anchor.
- Stop expanding a fix when it would require a new feature family, balance policy,
  or doctrine change.
- Stop after WP-001 handoff and branch push; do not begin Era 3 or WP-002.

## DELIVERABLES

- this work packet;
- any necessary bounded harness/runtime/test/documentation correction;
- an auditable row-by-row testability assessment;
- `docs/16-implementation/handoffs/WP-001-HANDOFF.md`;
- coherent local commits pushed to `origin/codex/wp-001-p3-evidence-followup`;
- draft PR when tooling permits, otherwise a recorded comparison/PR target.

## EXIT CRITERIA

COMPLETE when the current harness is sufficient for all remaining manual rows, or
when every necessary evidence-derived correction is implemented and tested, and the
handoff contains the minimal next physical Minecraft test batch.

BLOCKED when a remaining row exposes a real Anchor-owned design or scope decision.
