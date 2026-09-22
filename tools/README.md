# Project tooling

Status: PARTIAL — COMBAT/SIMULATION TOOLING EXISTS; PRODUCTION FACTORY TOOLING MOSTLY FUTURE

The plant requires tooling that is not gameplay.

Existing work already proves that JVM-side validation/simulation can carry important project logic without launching Minecraft.

The next major tooling era is broader: content, asset and world production must become safe enough to support hundreds or thousands of authored entries.

See:

- `docs/14-content-pipeline/ERA3_PRODUCTION_FACTORY_BOOTSTRAP.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`
- `docs/16-implementation/MASTER_PRODUCTION_TIMELINE.md`

## Planned tooling families

- content validator;
- deterministic compiler/generator;
- reference/index inspector;
- headless combat/progression/economy simulation;
- loot/content reports;
- quest dependency validation/reporting;
- asset exporters/checkers;
- animation/attachment validation where practical;
- world metadata exporters/inspectors;
- spawn/travel diagnostics;
- save/schema migration utilities;
- content/asset regression reports.

Validators and simulation should remain JVM-only where practical so CI can run them without launching Minecraft.

Tools become separate Gradle modules only when that produces a concrete dependency/execution benefit.

## Production-factory rule

Do not build editor applications from imagination merely because the project is large.

First prove the golden cross-domain package with command-line/build-integrated tooling. Measure where authoring/revision is painful. Build richer editors/visualizers only where the bottleneck is real.

The tooling goal is not 'lots of internal apps'.

It is:

**author source -> detect mistakes early -> inspect references -> produce deterministic runtime output -> revise safely.**
