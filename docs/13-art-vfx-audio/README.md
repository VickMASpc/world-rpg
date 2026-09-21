# Art, models, VFX and audio

Status: DRAFT

Editable source assets remain separate from shipped runtime exports where formats differ. Blockbench sources, layered textures, UI masters and audio masters stay source-of-truth.

Complex actor pipelines need stable model IDs, source model, runtime export, texture set, animation set, attachment points, scale/collision metadata and gameplay-to-animation mapping. Simple actors should remain simple when vanilla-compatible rendering is enough.

Gameplay effects reference presentation IDs rather than directly spawning arbitrary particles in combat code.

VFX definitions may specify emitters, duration, attachment, scale, sound, light, animation trigger, camera response and performance/LOD policy.

Changing Fireball visuals must not change Fireball damage.
