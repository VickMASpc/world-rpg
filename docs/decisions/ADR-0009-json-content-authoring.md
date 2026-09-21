# ADR-0009: Standard JSON for authored RPG definitions

Status: ACCEPTED

## Context

World RPG needs a content format that can scale to thousands of definitions, remain diffable in Git, be validated in CI, be packaged through Minecraft's server-data resource system, and be parsed by future standalone tooling without requiring a running Minecraft instance.

Path-derived identity would make file moves dangerous. Minecraft-only serialization types would also make headless tooling unnecessarily dependent on the game runtime.

## Decision

Authored RPG definitions use UTF-8 standard JSON.

Every top-level definition document carries at least:

- an explicit stable `id`,
- an explicit integer `schema` version.

File paths are provenance/organization, not identity.

The generic source/identity/validation model remains Minecraft-independent. A Fabric adapter will discover packaged server-data resources and feed them into the same loader used by standalone tools.

## Consequences

- Standard JSON tooling works everywhere.
- Generated definitions can be deterministic and stable-order.
- Moving a source file does not rename the persisted definition.
- JSON comments are not supported. Long-form authoring notes belong in adjacent documentation or explicit schema fields where semantically relevant.
- Duplicate object keys must eventually be rejected by structural validation rather than relying on permissive parser behavior.
- Domain decoders may use Gson at the platform/tooling edge, but core definition identity and registry types do not depend on Gson or Minecraft.
- JSON5/YAML/TOML are not part of the production definition pipeline unless this ADR is superseded.
