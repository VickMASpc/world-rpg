# Planning status

## Current phase

P0-P2 architecture bootstrap.

## Completed

- Repository initialized.
- Fabric 1.21.1 shell created.
- Java 21 and split main/client sources established.
- CI skeleton created.
- Documentation topology established.
- Initial architecture decisions recorded.

## Active

- Runtime package decomposition.
- Persistence ownership.
- Data loading and reload model.
- Registry/reference contracts.
- Dependency evaluation.
- Save schema/versioning rules.
- Validation architecture.

## Blocked from mass production

Classes, abilities, items, quests, NPC population, mob catalogs, professions, terrain production, final UI and final balance numbers.

Small fixtures are allowed only to exercise infrastructure.

## P2 exit gate

A definition can travel source -> decode -> validation -> reference resolution -> registry -> runtime inspection, and persisted references can survive schema evolution without ambiguity.
