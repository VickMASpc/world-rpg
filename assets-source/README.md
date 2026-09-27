# Asset source

Status: RESERVED EDITABLE MASTER ROOT — ERA 3 ASSET FACTORY NOT YET PROVEN

Editable source assets live here:

- Blockbench/model masters;
- source textures;
- animation masters;
- UI masters;
- icons;
- VFX source;
- audio masters.

Runtime exports may later be generated or copied into mod resources by tooling.

Do not make a shipped export the only editable copy of a complex asset.

See:

- `docs/13-art-vfx-audio/README.md`
- `docs/14-content-pipeline/ERA3_PRODUCTION_FACTORY_BOOTSTRAP.md`
- `docs/16-implementation/FIRST_PROVINCE_PRODUCTION_LEDGER.md`

## Rules

- source masters remain authoritative;
- runtime exports are reproducible where practical;
- asset IDs/references must be stable and validated;
- model scale/attachments/rig assumptions must be documented rather than guessed per asset;
- missing mandatory runtime exports should fail validation/build once the relevant factory exists;
- do not mass-produce models/animations/VFX/icons before the first source -> export -> runtime loop is stable;
- developer/golden-package assets do not silently become final visual style.

## Era 3 proof

The golden production package must prove at least one complete asset chain:

editable source master
-> validated/exported runtime asset
-> stable content/presentation reference
-> visible Minecraft runtime use
-> successful replacement/re-export during the revision test.

The same era must prove one animation path, one icon path, one VFX path and one audio path before first-province production volume ramps up.
