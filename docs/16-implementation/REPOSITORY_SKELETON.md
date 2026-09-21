# Repository skeleton

Status: ACCEPTED

~~~text
world-rpg/
├─ .github/workflows/
├─ docs/
│  ├─ 00-constitution/
│  ├─ 01-platform/
│  ├─ 02-runtime-architecture/
│  ├─ 03-data-model/
│  ├─ 04-combat-math/
│  ├─ 05-progression/
│  ├─ 06-items-loot-economy/
│  ├─ 07-classes-abilities/
│  ├─ 08-quests-dialogue/
│  ├─ 09-world-travel/
│  ├─ 10-npcs-mobs-ai/
│  ├─ 11-professions/
│  ├─ 12-ui-ux/
│  ├─ 13-art-vfx-audio/
│  ├─ 14-content-pipeline/
│  ├─ 15-testing-balancing/
│  ├─ 16-implementation/
│  └─ decisions/
├─ src/main/java/
├─ src/main/resources/
├─ src/client/java/
├─ content/
├─ assets-source/
├─ world-source/
├─ tools/
└─ generated/
~~~

docs is architecture/design source of truth.
src is runtime Fabric implementation.
content is human-authored RPG definition source.
assets-source keeps editable masters.
world-source owns fixed-world metadata/artifacts.
tools owns validators, compiler, simulator and exporters.
generated is disposable deterministic output.

## One module first

We keep one runtime module until a real boundary justifies a split. Likely triggers: tooling must run without Minecraft, shared model code needs a clean JVM artifact, compile isolation becomes valuable, or packaging diverges.

The headless tooling/simulator is the most likely first extraction.
