# Repository operating rules

This repository uses the Anchor -> Codex -> GitHub -> Minecraft evidence workflow.
Anchor owns product direction, packet selection, scope approval, physical-evidence
interpretation, and phase/gate decisions. Codex implements bounded packets locally,
tests and commits them, pushes the packet branch, and writes a durable handoff.
GitHub is the durable shared record. Minecraft and human testing provide physical
and lived evidence that automated checks cannot establish.

## Repository workflow

- Work in the local Git clone. Do not use GitHub as the primary file-editing mechanism.
- Before every packet, inspect the worktree, fetch `origin`, update the integration
  branch with a fast-forward-only pull, and record its current commit as `BASE_SHA`.
- Read the active Anchor state and work packet before substantial work.
- Create a dedicated `codex/wp-*` branch from the integration branch. Never implement
  a packet directly on the integration branch.
- Make coherent local checkpoint commits whenever losing the worktree would destroy
  meaningful completed work. Avoid meaningless microcommits.
- Push only the packet work branch. Never force-push unless the active packet
  explicitly authorizes history rewriting.
- Do not merge the work branch or advance another phase without explicit Anchor
  instruction.
- End every packet with a durable handoff and a clean, understood worktree.

The current integration branch, active gate, authorized work, and frozen work are in
[`docs/16-implementation/ANCHOR_STATE.md`](docs/16-implementation/ANCHOR_STATE.md).
Packet and handoff conventions live under `docs/16-implementation/work-packets/`
and `docs/16-implementation/handoffs/`.

## Project authority

Use this authority order:

1. explicit active work packet;
2. `docs/16-implementation/ANCHOR_STATE.md`;
3. accepted constitution and doctrine;
4. architectural documents;
5. implementation code and supporting documentation.

A lower layer must not silently redefine a higher layer. Escalate real conflicts in
the handoff instead of resolving them by inventing doctrine.

## Idea fidelity

Preserve World RPG's identity:

- it is a huge authored RPG using Minecraft as an engine;
- levels 1-100 are the central game;
- progression is extremely slow and lived-in;
- geography and travel matter;
- long expeditions and quiet stretches are valid;
- towns and inns matter;
- equipment lasts long enough to become memorable;
- quests can persist across sessions and days;
- old places age gradually rather than disappearing;
- architecture exists to serve the game.

Do not optimize these qualities away for modern convenience.

## Scope discipline

- Implement the active packet only.
- Do not add abstractions merely because they would be elegant.
- Do not perform unrelated cleanup.
- Do not invent gameplay doctrine to solve implementation discomfort.
- Do not advance another phase automatically when a packet ends.
- Record out-of-scope opportunities as follow-up candidates.

## Gate discipline

- Never weaken a test or gate expectation merely to obtain green results.
- Preserve failed evidence and fix the layer that owns the failure.
- Proof-fixture numbers are not production balance.
- Synthetic simulator fixtures do not automatically become gameplay doctrine.
- Never claim physical Minecraft evidence that was not physically observed.

## Production-scale discipline

Framework progress is not game completion. Every substantial progress report must
distinguish:

1. what additional capability the production plant can express;
2. what additional part of the actual RPG exists in authored, playable, or
   presentable form.

Read `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md` and
`docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md` before making broad
project-progress claims. The architecture phase list is not a completion percentage.
