# WP-001 handoff

WORK PACKET:
`WP-001 — P3 Physical Evidence Follow-up`

RESULT:
COMPLETE

INTEGRATION BRANCH:
`p4/math-simulator`

BASE SHA:
`45c5d2030920fc22c5dc9238de90c1d28830cddf`

WORK BRANCH:
`codex/wp-001-p3-evidence-followup`

HEAD SHA:
`9539dde7dc6ed86ee0497e7d02d5a87b630391f8` (last packet checkpoint before this
administrative handoff commit)

## COMMITS

- `9539dde docs: establish local work packet workflow`
- `docs(p3): record evidence follow-up assessment` (this handoff commit)

## FILES CHANGED

- `AGENTS.md`
- `docs/16-implementation/ANCHOR_STATE.md`
- `docs/16-implementation/work-packets/README.md`
- `docs/16-implementation/work-packets/WORK_PACKET_TEMPLATE.md`
- `docs/16-implementation/work-packets/WP-001-P3-EVIDENCE-FOLLOWUP.md`
- `docs/16-implementation/handoffs/README.md`
- `docs/16-implementation/handoffs/HANDOFF_TEMPLATE.md`
- `docs/16-implementation/handoffs/WP-001-HANDOFF.md`

## WHAT CHANGED

- Established the permanent local Git, packet, authority, scope, gate, handoff,
  idea-fidelity, and production-scale rules required by the Anchor.
- Recorded the current Anchor state and exact integration baseline.
- Created and completed the bounded WP-001 packet.
- Audited the physical matrix, latest evidence, runbook, authored proof abilities,
  target-observation adapter, cast lifecycle, developer room, client/network path,
  cooldown/replay handling, and relevant automated tests.
- Determined that the existing harness is sufficient for every remaining manual row.
  No current evidence proves a runtime/kernel defect, and no additional developer
  command is required before the next physical run.

### Remaining-row testability assessment

| Row | Assessment | Current proof path |
| --- | --- | --- |
| G1 | Executable as documented | `room reset`, `room spawn 3`, and `room status` initialize and display distinct player/target actor state. |
| G3 | Executable as documented | Status during and after an accepted F7 Bolt exposes committed mana and target health. |
| G5 | Executable as documented | Source position is captured at cast start; movement produces `MOVEMENT` before cast resolution. |
| G6 | Executable as documented | `room move 20` relocates the same UUID without replacing its RPG state; timed completion re-observes range. |
| G7 | Executable as documented | `room cast bolt` uses the room target directly, reports network bypass, and returns the first server condition failure. |
| G8 | Executable as documented | Timed completion re-observes live Minecraft LOS before applying the Bolt effect. |
| G9 | Executable as documented; earlier evidence remains ambiguous | Bolt data has a 12-block maximum; stationary `spawn 20` plus direct cast isolates server range without relying on crosshair reach. |
| G10 | Executable as documented | Fresh F8 Channel emits three separately scheduled resource events and completion. |
| G11 | Executable as documented | Movement interrupts the active channel before subsequent server ticks can resolve. |
| G12 | Executable as documented | Fresh status exposes +10 item power; F6 applies the +20 aura in the same stat sheet. |
| G13 | Executable as documented | `room health 0` changes only RPG health; F7 then validates the RPG target-resource condition while the Husk remains present. |
| G14 | Executable as documented | F6 starts both GCD and Focus cooldown; immediate F7 and delayed F6 expose the two independent rejections. |
| G15 | Already automated | `P3RequestSequenceTrackerTest` proves duplicate/older-sequence rejection. |
| G16 | Executable as documented | Reset discards the target and removes both target/player P3 state; respawn rebuilds fresh state. |
| G17 | Executable as documented | The Husk entity selector feeds live Minecraft distance, LOS, and facing observations to the probe summary. |
| G18 | Existing candidate PASS; current branch locally green | Full local build passed; remote CI for this pushed branch remains GitHub-side evidence. |

Rows G2 and G4 already have recorded physical passes. No neighboring runtime code was
changed, so WP-001 does not require those passes to be discarded or rerun.

## WHAT DID NOT CHANGE

- No Java, JSON fixture, network, runtime, kernel, command, or test code changed.
- Bolt remains an authored 12-block proof fixture.
- No proof value became production balance or gameplay doctrine.
- No evidence row was weakened, upgraded from static inspection, or reclassified as
  a new manual PASS.
- No P4 feature family, gameplay mechanic, class, content volume, or Era 3 work was
  started.

## TESTS EXECUTED

- Command:
  `./gradlew.bat test --tests "dev.worldrpg.combat.P3KernelProofTest" --tests "dev.worldrpg.combat.cast.CastObservationRevalidationTest" --tests "dev.worldrpg.combat.cast.CastControllerValidationTest" --tests "dev.worldrpg.combat.target.TargetConditionsTest" --tests "dev.worldrpg.content.combat.CombatContentDomainsTest" --tests "dev.worldrpg.network.p3.P3RequestSequenceTrackerTest" --stacktrace`
- Result: PASS — `BUILD SUCCESSFUL in 1m 42s`; 5 actionable tasks executed.
- Command: `./gradlew.bat build --stacktrace`
- Result: PASS — `BUILD SUCCESSFUL in 17s`; 10 actionable tasks, 6 executed and 4 up-to-date.
- Build note: the focused run emitted an existing Fabric API duplicate-input-class
  warning during configuration; it did not fail compilation or tests.

## PHYSICAL / BEHAVIORAL EVIDENCE

WP-001 performed repository inspection and automated verification only. It did not
run Minecraft or observe new physical behavior. The matrix statuses in
`P3_PHYSICAL_EVIDENCE_2026-09-22.md` therefore remain authoritative.

### Minimal next physical Minecraft test batch

Run G9 alone first because it resolves the only observation that could be mistaken
for a range defect:

```text
/worldrpg p3 room reset
/worldrpg p3 room spawn 20
# DO NOT MOVE
/worldrpg p3 room cast bolt
/worldrpg p3 room status
```

Record the full direct-activation response and status. Expected:

- response explicitly includes `(network path bypassed)`;
- rejection identifies the 12-block maximum-range condition;
- player mana remains 100;
- target proof health remains 100;
- no cast is active.

Do not reinterpret a different physical result. Preserve it and return it as failure
evidence for an owning-layer fix.

After G9, the next compact closure batch is G1/G3 from one fresh Bolt run, then fresh
G10, fresh G12, and the correct Husk G17 probe. The authoritative steps remain in
`P3_PHYSICAL_EVIDENCE_RUNBOOK.md`.

## IDEA / DESIGN IMPACT

Plant capability: no new capability was added; WP-001 establishes that the existing
proof plant can express and observe every remaining matrix row without expansion.

Actual RPG: no additional authored, playable, or presentable RPG content was created.
This packet only increases confidence in the mechanism used to prove future combat
behavior inside Minecraft.

## DEVIATIONS FROM PACKET

- GitHub issue #3 was not read through GitHub CLI because `gh` is not installed.
  The current relevant state was taken from repository authority documents as the
  packet explicitly permits.
- No code correction was made because the investigation found no evidence-derived
  defect or missing command.

## KNOWN RISKS

- Static inspection and passing unit/build checks cannot establish Minecraft physical
  PASS for any pending row.
- G7/G8 still depend on the human placing a blocker that actually intersects the
  eye-to-eye LOS ray; preserve probe/status output if placement is uncertain.
- G11 timing depends on moving immediately after the observed tick; chat chronology,
  rather than an assumed final health value, remains the decisive evidence.
- Current-branch G18 remote CI evidence does not exist until GitHub runs CI for the
  pushed branch or PR.

## UNRESOLVED

- P3 remains open. Physical statuses remain exactly as recorded in the current
  verification matrix and 2026-09-22 evidence file.
- G9 and G17 still require the controlled physical procedures above/runbook.
- G1/G3/G6/G10/G12 remain partial until fresh physical output closes them.
- G5/G7/G8/G11/G13/G14/G16 remain physically pending.

## FOLLOW-UP CANDIDATES

- Execute and record the minimal G9 physical batch.
- If G9 matches the expected rejection, execute the compact partial-closure batch:
  G1/G3, G10, G12, and G17.
- Continue the remaining interruption/LOS/lifecycle rows in runbook order after the
  Anchor reviews the resulting evidence.

These are candidates for Anchor sequencing, not authorization for Codex to start a
new packet.

## WORKTREE STATUS

Expected clean after the handoff commit; verified again before push.

## PUSH STATUS

Pending final handoff commit and branch push.

## PR

Not created in this packet environment because GitHub CLI is unavailable. After push,
the review target is:

- base: `p4/math-simulator`
- head: `codex/wp-001-p3-evidence-followup`
- compare: `https://github.com/VickMASpc/world-rpg/compare/p4/math-simulator...codex/wp-001-p3-evidence-followup?expand=1`
