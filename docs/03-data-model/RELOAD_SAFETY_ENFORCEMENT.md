# Reload-safety enforcement

Status: IMPLEMENTED

ReloadSafety is enforced against the currently published snapshot.

Initial server-data publication is never blocked by live-reload policy.

For later reloads, each changed definition domain is classified:

## SAFE

A validated changed registry may publish live.

## GUARDED

A changed registry requires a registered domain reload guard.

If no guard exists, publication is rejected.

A guard receives the active and candidate registries plus the shared validation
report.

The guard permits the change by adding no error. It rejects the change by
adding a validation error with an owning-domain explanation.

## RESTART

Any semantic change to the registry is rejected during live reload.

The current active snapshot remains published.

The new content may be used after restart/save reload, where it becomes the
initial publication.

## Change detection

Reload policy compares immutable definition maps.

Moving files or changing provenance without changing the decoded definitions
does not count as a semantic registry change.

## Atomicity

Reload-safety errors participate in the same candidate ValidationReport as
decode, reference, semantic and cross-registry errors.

No partial registry replacement occurs.
