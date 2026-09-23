# Packet handoffs

Every work packet ends with a durable handoff named `WP-###-HANDOFF.md`. The
conversation is not the record of completion. The handoff lets the Anchor review the
exact base, branch, commits, changed files, verification, physical evidence, risks,
and unresolved decisions.

Use `HANDOFF_TEMPLATE.md`. Record exact commands and results. Distinguish automated
verification from physical Minecraft observation, and never promote unobserved manual
behavior to PASS. State both production-plant capability and actual authored/playable/
presentable RPG impact when reporting material progress.

Every handoff records both:

- `IMPLEMENTATION HEAD` — the substantive completed packet commit before any purely administrative tail, when such a distinction exists;
- `FINAL BRANCH HEAD` — the exact pushed remote SHA presented for Anchor review, PR and CI.

Anchor acceptance and CI always refer to `FINAL BRANCH HEAD`.

Before finalizing the handoff, inspect `git status` and
`git log --oneline <BASE_SHA>..HEAD`, push only the work branch, and record PR status.
Leave the worktree clean and understood. Do not self-merge or begin another packet.