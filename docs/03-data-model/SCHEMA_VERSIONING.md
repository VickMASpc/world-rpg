# Definition schema versioning

Status: ACCEPTED for P2

Authored definition documents carry an integer `schema` field with a minimum value of 1.

Schema version belongs to the definition format, not to the character's gameplay level or content revision.

## Rules

- Each definition domain declares a current schema version.
- A source with the current version is decoded normally.
- An older supported version is migrated in-memory before semantic validation/publication.
- An older unsupported version is a validation error.
- A newer version than the runtime understands is a validation error; guessing is forbidden.
- Migration never changes stable definition identity unless an explicit ID migration table says so.
- Definition schema versions and persisted save schema versions are separate systems.

## Example

```json
{
  "schema": 1,
  "id": "world_rpg:ability/mage/frostbolt"
}
```

This header is intentionally small. Domain-specific fields are decoded only after the common document header is valid.
