# Work packets

Every implementation task after the workflow bootstrap requires a durable, bounded
work packet. The Anchor may choose a task conversationally, but Codex must resolve it
into a packet before substantial implementation. “Continue development” alone is not
an executable packet.

Before starting a packet, Codex must:

1. read `../ANCHOR_STATE.md` and the active packet;
2. inspect the worktree;
3. fetch `origin`, switch to the named integration branch, and pull with
   `--ff-only`;
4. record the integration head as `BASE_SHA` and reconcile any difference from the
   Anchor-known SHA without resetting or rewriting history;
5. create the packet's dedicated `codex/wp-*` branch.

Use `WORK_PACKET_TEMPLATE.md`. The packet must define objective, authority, scope,
invariants, required evidence, stop conditions, deliverables, and exit criteria.
Implementation stays within that boundary. New findings become follow-up candidates
unless they are necessary to satisfy the active packet.

At exit, commit coherent work, run the required checks, push only the work branch,
and create the matching handoff under `../handoffs/`. Do not self-merge or begin the
next packet.
