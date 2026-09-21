# Platform baseline

Status: ACCEPTED for bootstrap

## Engine

- Minecraft Java 1.21.1.
- Java 21.
- Fabric Loader/toolchain.
- Yarn mappings.
- Gradle + Fabric Loom.
- One primary runtime mod JAR initially.

Minecraft 1.21.1 is frozen as the production engine target. Moving Minecraft versions is an engine migration, not routine maintenance.

## Bootstrap pins

- Yarn 1.21.1+build.3.
- Fabric Loader 0.16.14.
- Fabric API 0.116.17+1.21.1.
- Fabric Loom 1.11.8.
- Gradle 8.14.3.

These pins provide reproducibility and may be patched after clean-build/regression checks while Minecraft remains fixed. Loom 1.11.8 requires Gradle 8.14 or newer; 8.14.3 is the pinned bootstrap patch release.

## Source sets

Common/server-safe code lives under src/main. Client rendering, screens, input and presentation live under src/client. Common code must not acquire accidental client-only references.

## Dependency admission

Every non-Fabric dependency must document the exact problem solved, coupling, 1.21.1 support, runtime footprint, save/data implications, replacement strategy and compatibility impact.

Current candidates, not admitted yet:

- Cardinal Components API for persistent attached state.
- GeckoLib for complex animation.
- owo-lib / owo-ui for UI primitives.

Core RPG semantics remain owned by World RPG.
