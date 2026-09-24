# JSON strictness

Status: ACCEPTED

World RPG authored definitions use strict JSON.

Before Gson creates a `JsonObject`, P2 performs a streaming structural pass.

## Rejected structure

The preflight rejects:

- malformed JSON,
- trailing root content,
- duplicate object member names at any nesting depth.

Example:

```json
{
  "id": "world_rpg:item/a",
  "id": "world_rpg:item/b"
}
```

is invalid.

World RPG will not silently pick one value.

The same rule applies inside nested objects such as effects, conditions, loot
entries, dialogue nodes, or future item stat blocks.

## Why

Duplicate keys are especially dangerous in a project designed to contain
thousands of authored definitions.

Different parsers, tools, formatters, or editors may disagree about which
duplicate value wins.

Allowing that ambiguity would make source review lie about runtime behavior.

## Diagnostics

Duplicate-key errors include the logical JSON path, for example:

```text
$.effects[2].amount
```

Physical source provenance continues to identify the resource pack/file.

Exact line/column enrichment remains a future diagnostics improvement.
