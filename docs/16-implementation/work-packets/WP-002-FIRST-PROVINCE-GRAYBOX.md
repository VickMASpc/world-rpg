# Work packet

ID: WP-002

TITLE: First-Province Graybox Generator

STATUS: ACTIVE

INTEGRATION_BRANCH: `p4/math-simulator`

EXPECTED_BASE_SHA: `c9b7816b8994d16365e5d53d55521f05d66fe55c`

WORK_BRANCH: `codex/wp-002-province-graybox`

## OBJECTIVE

Make the accepted first-province topology physically walkable in a disposable Minecraft test world. Add a permission-gated developer command that builds a deliberately crude route network and write the human walk-test runbook needed to measure it.

This packet prepares the topology for physical validation. It does not claim the topology has passed that validation.

## WHY THIS ADVANCES THE RPG

World RPG's geography, routes, quiet stretches, settlement separation, remembered danger, and learned shortcuts must work as physical space. A walkable graybox gives the Anchor real route-time and navigation evidence before any final terrain or settlement art is produced.

The graybox is test infrastructure plus a small playable spatial prototype. It is not production terrain or first-province content.

## AUTHORITATIVE DOCUMENTS

- `AGENTS.md`
- `docs/16-implementation/ANCHOR_STATE.md`
- `docs/STATUS.md`
- `docs/00-constitution/SPIRIT_OF_THE_GAME.md`
- `docs/00-constitution/IDEA_FIDELITY_PACT.md`
- `docs/00-constitution/ANTI_PATTERN_CATALOG.md`
- `docs/00-constitution/P4_G_RECONVERGENCE_GATE.md`
- `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`
- `docs/09-world-travel/FIRST_PROVINCE_TOPOLOGY_PROOF.md`
- `docs/16-implementation/P3_GATE_VERIFICATION.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_RUNBOOK.md`
- `docs/16-implementation/P3_PHYSICAL_EVIDENCE_2026-09-22.md`

## IN SCOPE

- A developer-only, operator-level command to build the topology graybox in a dedicated fresh Superflat Overworld.
- A fixed, explicitly provisional 1:1 block-coordinate layout anchored at the command user's current position, with +X east and +Z south.
- Cheap, readable paths and landmarks for home A, local wild B, main corridor C, fork/bridge D, early danger pocket E, remote refuge F, regional settlement G, and outward threshold H.
- The topology's relevant route choices: local ruin R1, work/resource detour B2, dangerous shorter route, and a narrower learned G-to-A return shortcut.
- Primitive bridge/river and landmark geometry only where needed to make the physical route decisions legible.
- A walk-test runbook that records the route, elapsed walking time, wrong turns, landmark recall, and subjective local/journey/remote feel.
- Focused tests that verify the route geometry preserves the accepted provisional travel bands and relative shortcut savings.

## OUT OF SCOPE

- Final terrain, biome, settlement, dungeon, or architectural production.
- NPCs, mobs, encounters, combat, quests, services, resources, loot, or other gameplay content.
- Changes to P3/P4 runtime mechanics, combat mathematics, classes, progression, or balance.
- Treating the topology's provisional minute bands as doctrine-level constants.
- Claiming physical navigation or route-time PASS before a human walks and records the graybox.
- Starting Era 3, WP-003, or any subsequent packet.

## INVARIANTS

- Preserve the accepted abstract graph and lived-travel intent in `FIRST_PROVINCE_TOPOLOGY_PROOF.md`.
- The generator is disposable developer tooling; it must not become final-world generation or production content.
- The generated space must retain quiet travel and meaningful route decisions; do not fill it with encounters or rewards to hide emptiness.
- Walk-time targets remain provisional measurement targets and are revised from evidence.
- The command must be server-side and permission-gated, and its overwrite area must be explicitly described.
- P3 G1-G18 remains a separate evidence gate; topology work does not close or replace it.
- Architecture and tooling progress must be reported separately from authored/playable/presentable RPG content.

## KNOWN EVIDENCE

- The accepted topology proof contains the A/B/C/D/E/F/G/H roles, provisional walking-time bands, route-choice requirements, mental-map landmarks, quiet-space requirement, and measurement sheet.
- The topology is currently an authored paper hypothesis, not a physically walked map.
- P3 G9 remains pending independently; the immediate P3 procedure is still the controlled stationary 20-block Bolt rejection test.
- The topology prototype must remain cheap and disposable. No final art investment is authorized.

## REQUIRED WORK

1. Implement the developer-only builder and command using only the topology needed for this packet.
2. Use the user's command-time position as node A; print the origin/orientation and a color/marker legend without changing player state or teleporting the player.
3. Keep the builder safe to rerun within the dedicated disposable world and document that it replaces surface blocks along its paths and small landmarks.
4. Add focused geometry tests for the route distances, route connections, and meaningful learned-shortcut savings.
5. Add `docs/16-implementation/FIRST_PROVINCE_GRAYBOX_RUNBOOK.md` with exact setup and a minimal physical route batch, based on the existing topology measurement sheet.
6. Do not add any unrelated abstractions or edit constitution/doctrine.

## TESTS / EVIDENCE REQUIRED

- Run the focused graybox layout tests first.
- Run `./gradlew test` and `./gradlew build` before handoff.
- Record the exact commands/results and the GitHub CI result on the final branch head.
- State clearly that no physical Minecraft route evidence was obtained by Codex.
- Give the user the exact next route batch to walk and the fields to record.

## STOP CONDITIONS

- If the implementation requires changing the accepted route graph, travel doctrine, or target bands, stop and report the conflict for Anchor decision.
- If a usable builder would require broad world-generation infrastructure, production POI schemas, or unrelated runtime changes, stop and report the smallest unresolved requirement.
- If route lengths cannot meet the provisional measurement bands without distorting the topology, record that as evidence; do not move targets silently.
- Do not start Era 3 or another packet after this one exits.

## DELIVERABLES

- Developer-only graybox generation command and route layout.
- Focused route-geometry tests.
- `docs/16-implementation/FIRST_PROVINCE_GRAYBOX_RUNBOOK.md`.
- `docs/16-implementation/handoffs/WP-002-HANDOFF.md`.
- Pushed `codex/wp-002-province-graybox` branch and a draft PR against `p4/math-simulator`.

## EXIT CRITERIA

- The graybox can be built in a fresh dedicated Superflat Overworld without affecting a production world or player RPG state.
- A/B/C/D/E/F/G/H, local/resource detours, a dangerous shorter path, the bridge/river landmark, and learned return shortcut are physically present as cheap geometry.
- Automated route tests establish the actual block-distance relationships against the provisional topology bands.
- Focused tests, full project tests, full build, and CI pass on the exact final branch head.
- The runbook provides a minimal human test batch; no physical PASS is claimed until that evidence is returned and interpreted by the Anchor.
- Handoff is complete, branch is pushed, PR is draft, and no merge or later packet is started.
