# P4 production magnitude effect integration

Status: CORE RUNTIME PATH IMPLEMENTED

Production damage/healing can now travel through the generic P3 effect and cast systems without duplicating P4 mathematics.

## Runtime path

AbilityDefinition -> CastController -> EffectSequence preflight -> CombatMagnitudeEffect -> CombatResolutionGateway.validate -> cast/cost/cooldown lifecycle -> CombatMagnitudeEffect.apply -> CombatResolutionGateway.resolve -> P4 math -> authoritative resource mutation -> semantic/resource events.

CombatMagnitudeEffect owns no hit, crit, mitigation or scaling formula.

It describes recipient, DAMAGE/HEALING, cause ID, school ID, resolution-profile ID and authored base magnitude, then delegates.

## Preflight

CombatResolutionGateway remains a functional interface and now has a default pure validation method.

ProfiledCombatResolver overrides it to check predictable runtime/configuration failures before RNG or mutation:

- resolution profile exists;
- target owns the configured health resource;
- damage school is supported by the current resistance vocabulary;
- attacker level is valid for the mitigation-scale curve.

P3 EffectSequence validates every effect before applying any effect. Therefore an invalid magnitude effect prevents earlier effects in the same sequence from mutating state.

## Timed-cast proof

A regression test now proves that a timed P3 ability spends its cost at activation, leaves target health unchanged while casting, completes at the scheduled tick, invokes the P4 production resolver, mutates target health, and emits the semantic magnitude event.

## Authored-data boundary

The runtime effect exists, but authored AbilityEffectSpec does not yet expose a resolve-magnitude JSON effect.

Doing that correctly requires the combat-content compiler/runtime to receive the active production resolution gateway. We will not fake that dependency with a global singleton.