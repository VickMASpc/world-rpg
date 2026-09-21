# World RPG

A slow-burn, data-driven fantasy RPG total conversion for **Minecraft Java 1.21.1 + Fabric**.

World RPG is intentionally not designed as a normal Minecraft survival mod. Minecraft provides the engine, networking model, rendering base, world format, entity framework, and tooling surface; World RPG replaces the progression loop with an authored RPG built around a fixed world, long-distance travel, 100-level character growth, deliberate cast-heavy combat, persistent quests, professions, reputation, equipment, dungeons, and hundreds of hours of first-character progression.

## Status

**Pre-production / plant architecture.**

The current priority is to design and implement the production framework before mass-producing content. The project will not create hundreds of abilities, thousands of items, or a continent until the systems that define, validate, simulate, migrate, and debug that content are stable.

## Core constraints

- Minecraft Java **1.21.1** is the frozen engine target unless an explicit architecture decision changes it.
- Fabric is the mod loader/toolchain.
- The logical server is authoritative for gameplay state and outcomes.
- Systems live in code; game content is data-driven.
- Static definitions are separate from runtime/player/world state.
- Stable namespaced IDs are used for cross-content references.
- The fixed RPG world is a first-class project artifact, not disposable procedural terrain.
- Slow progression must come from space, depth, travel, and long-form character growth rather than arbitrary punishment.
- Build-time validation and simulation are required before content scale explodes.

## Repository map

```text
world-rpg/
├─ docs/             Architecture, design bibles, ADRs, implementation plans
├─ src/              Fabric runtime source
├─ content/          Authored RPG content source
├─ assets-source/    Editable source assets: Blockbench, textures, UI, audio, VFX
├─ world-source/     Fixed-world authoring source and world metadata
├─ tools/            Validators, compiler/exporters, simulators and authoring support
├─ generated/        Generated outputs; never hand-authored
└─ .github/          CI and repository automation
```

See [docs/README.md](docs/README.md) for the documentation map once the architecture bootstrap lands.

## Build target

The repository is being bootstrapped around Java 21 and a Fabric 1.21.1 runtime. A Windows `build.bat` entrypoint is part of the skeleton so local testing stays one command.

## License

Not selected yet. Do not assume permission to redistribute project assets or source until a license is explicitly chosen.
