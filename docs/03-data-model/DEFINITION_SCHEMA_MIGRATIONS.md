# Definition schema migrations

Status: IMPLEMENTED

Authored definition domains may register explicit JSON migrations from older
schema versions to their current schema.

## Rules

- Each migration advances exactly one version.
- Only one migration may exist for a given source version in a domain.
- Missing migration steps reject the candidate.
- Newer-than-runtime schemas reject the candidate.
- Migrations operate on a defensive JSON copy.
- The loader owns the top-level schema field and updates it after each step.
- A migration may not change the registry-domain ID or stable definition ID.
- After migration, the normal domain decoder, reference resolver and validators
  run exactly as they do for natively-current source.

## Why migrations are code-registered

Definition migrations are trusted data-shape upgrades.

They are not a general-purpose scripting mechanism and are not authored inside
content packs.

## Atomicity

A migration failure is a normal candidate validation failure.

The active last-known-good registry snapshot remains published.
