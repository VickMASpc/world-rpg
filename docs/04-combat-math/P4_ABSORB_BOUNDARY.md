# P4 absorb boundary

Status: RESOLVER HOOK IMPLEMENTED — SHIELD STORAGE MODEL OPEN

Absorb now has an explicit place in production damage ordering:

contact -> scaling -> crit -> mitigation -> incoming modifier -> absorb -> health.

CombatAbsorbGateway is injected into ProfiledCombatResolver.

The default gateway absorbs nothing, preserving existing behavior.

A production gateway may own/mutate shield or barrier state, but runtime and simulator must use the same implementation.

CombatAbsorbResult returns incoming, absorbed and remaining damage with an invariant that absorbed + remaining equals incoming.

CombatResolutionTrace now records beforeAbsorb and absorbed separately from requestedFinal health damage.

When absorbed damage is positive, the resolver emits CombatAbsorbedEvent in addition to the resolved magnitude and resource-change events.

Healing bypasses the damage absorb gateway.

## Deliberately not frozen yet

This tranche does not choose:

- whether shields live in AuraContainer or a dedicated absorb container;
- stacking/priority ordering across multiple shields;
- school-specific shield filters;
- shield duration/refresh semantics;
- dispel interaction;
- item/talent shield budgets.

Those are state/content semantics. The resolver hook is now stable enough that those choices do not require changing damage topology.