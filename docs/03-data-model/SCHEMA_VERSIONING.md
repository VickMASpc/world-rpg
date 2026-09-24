# Definition schema versioning

Status: ACCEPTED for P2

Authored definition documents carry a common header:

```json
{
  "schema": 1,
  "registry": "world_rpg:registry/abilities",
  "id": "world_rpg:ability/mage/frostbolt"
}
```

`schema` is an integer with a minimum value of 1.

`registry` identifies the typed definition domain.

`id` is the durable definition identity.

File location is provenance, not identity.

## Rules

- Each definition domain declares a current schema version.
- A source with the current version is decoded normally.
- An older supported version will eventually be migrated in-memory before semantic validation/publication.
- Until a migration exists, an older version is rejected rather than guessed.
- A newer version than the runtime understands is a validation error.
- Migration never changes stable definition identity unless an explicit ID migration table says so.
- Definition schema versions and persisted save schema versions are separate systems.
