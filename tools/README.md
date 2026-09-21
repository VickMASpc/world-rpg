# Project tooling

The plant requires tooling that is not gameplay.

Planned tools include a content validator, compiler/generator, reference/index inspector, headless combat/progression simulator, asset exporters/checkers, world metadata exporters and save/schema migration utilities.

Validators and simulation should be JVM-only where practical so CI can run them without launching Minecraft.

Tools become separate Gradle modules when that produces a concrete dependency/execution benefit.
