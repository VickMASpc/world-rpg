# WP-002 Handoff — First-Province Graybox Generator

WORK PACKET: WP-002 — First-Province Graybox Generator

RESULT: PARTIAL — implementation is present; local build verification and the human walk-test remain pending.

INTEGRATION BRANCH: `p4/math-simulator`

BASE SHA: `c9b7816b8994d16365e5d53d55521f05d66fe55c`

WORK BRANCH: `codex/wp-002-province-graybox`

IMPLEMENTATION HEAD: `59f44c7117607929a825033774551836c80ef59d`

FINAL BRANCH HEAD: the current head of `codex/wp-002-province-graybox`; PR #13 is the canonical live record of its exact SHA and checks. The handoff does not attempt to embed its own commit hash.

## Commits

- `397648a` — activate WP-002 and its scope.
- `59f44c7` — add the graybox builder, layout, command, geometry tests, and walk-test runbook.

## Files changed

- `src/main/java/dev/worldrpg/command/WorldRpgCommands.java`
- `src/main/java/dev/worldrpg/integration/minecraft/ProvinceGrayboxBuilder.java`
- `src/main/java/dev/worldrpg/integration/minecraft/ProvinceGrayboxLayout.java`
- `src/test/java/dev/worldrpg/integration/minecraft/ProvinceGrayboxLayoutTest.java`
- `docs/16-implementation/FIRST_PROVINCE_GRAYBOX_RUNBOOK.md`
- `docs/16-implementation/work-packets/WP-002-FIRST-PROVINCE-GRAYBOX.md`
- `docs/16-implementation/ANCHOR_STATE.md`

## What changed

- Added the permission-level-2 command `/worldrpg province graybox build confirm`.
- The builder uses the command user's current location as A, rejects non-Overworld use and sampled uneven routes, and draws only disposable routes, markers, a shallow river, and a bridge. It does not teleport the player or edit RPG state.
- Added focused layout checks for route lengths, travel bands, local detours, and shortcut savings.
- Added setup, route, measurement, and evidence-boundary instructions. Added Windows build instructions and documented the approximate 9.8 km by 5.2 km sparse route-network bounds.
- Reaffirmed that a generated graybox and automated geometry checks are not physical navigation evidence and do not authorize final terrain work.

## What did not change

- No production terrain, settlement, NPC, mob, quest, reward, class, combat, or progression content was authored.
- No final travel-time or province-size doctrine was set.
- The P3 G1-G18 matrix, including the controlled G9 stationary range test, remains a separate open physical gate.
- No later work packet or Era 3 production work was started.

## Tests executed

- `./gradlew test --tests dev.worldrpg.integration.minecraft.ProvinceGrayboxLayoutTest` — BLOCKED before Gradle startup. The wrapper could not download Gradle 8.14.3 (`java.net.SocketException: Network is unreachable`).
- `./gradlew test` — not run; same environment limitation.
- `./gradlew build` — not run; same environment limitation.
- Workspace Java is 17.0.20; the project requires Java 21, and this workspace has no Gradle installation or Java 21 compiler.
- GitHub Actions run `35987477483` started for the initial handoff head `7f767206707f118b251c74933536b99a9e16ebe9`; it was still running when this handoff was updated. The final docs-only head receives its own PR check. PR #13 records the exact final head and check result.

## Physical / behavioral evidence

- No Minecraft run or route walk was observed by Codex.
- No physical PASS is claimed for any route, landmark recall, shortcut value, danger communication, or quiet-space quality.
- Required next action: build on Minecraft 1.21.1 / Fabric, create a fresh dedicated Superflat Overworld, and follow `FIRST_PROVINCE_GRAYBOX_RUNBOOK.md`. Return the measured route sheet; the Anchor interprets the evidence.
- P3 physical evidence remains separate; G9 still requires the controlled stationary range-rejection procedure in `P3_PHYSICAL_EVIDENCE_RUNBOOK.md`.

## Idea / design impact

The new development capability makes the accepted route hypothesis physically walkable before investment in final terrain. It creates no authored slice content; the added gameplay remains a small spatial test fixture.

The attached revised scale proposal describes the first slice as approximately 1.5 x 1.5 km, while the current sparse WP-002 route network spans approximately 9.8 km east-west by 5.2 km north-south. The route geometry is explicitly provisional, and its bounding extent is not filled map area. This discrepancy must be reconciled before this graybox is treated as representative of the final province footprint; do not silently shrink the accepted travel bands or treat them as settled final dimensions.

## Deviations from packet

- Local focused/full Gradle checks cannot run in this workspace because the required Gradle distribution and Java 21 toolchain are unavailable. CI must verify the pushed PR head.
- This handoff records the updated slice-scale question without changing the accepted topology graph or its route targets.

## Known risks

- The builder writes a large number of blocks synchronously and traverses a sparse route network across many chunks. Run it only in the dedicated disposable Superflat world described by the runbook.
- The test bounds come from a provisional 250 blocks/minute reference. Measured human walking time and lived navigation remain authoritative.
- The graybox uses highly visible colored blocks and markers; it cannot establish how final terrain, landmarks, routes, danger cues, or settlement identity will read.

## Unresolved

- Physical walk-test results and Anchor interpretation.
- Reconciliation of the revised ~1.5 x 1.5 km opening-slice scale with the current 9.8 x 5.2 km route-network bounds.
- GitHub Actions result on the exact final branch head.

## Follow-up candidates

- Record the human graybox route measurements and revise topology only under Anchor direction.
- Complete the distinct P3 physical evidence rows without weakening any expectation.
- Reconcile the first-province production ledger after physical topology evidence.

## Worktree and push status

- Worktree status: clean after syncing to the remote packet branch.
- Push status: the handoff and runbook changes are present on remote branch `codex/wp-002-province-graybox`. The local CLI push could not prompt for credentials; the reviewed local file content was transferred through the connected GitHub write path.

## PR

- Draft PR: [#13 — WP-002: First-Province Graybox Generator](https://github.com/VickMASpc/world-rpg/pull/13), against `p4/math-simulator`. It remains open and unmerged.

## Head semantics

- `IMPLEMENTATION HEAD` is the commit containing the substantive WP-002 implementation.
- `FINAL BRANCH HEAD` is the exact pushed branch SHA presented for review and CI.
- The final PR checks must be read against that exact SHA. No branch merge is part of this handoff.
