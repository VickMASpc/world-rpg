# P3 developer input

Status: TEMPORARY

P3 now has a disposable client input surface whose only purpose is to exercise
the real client -> server activation protocol.

## Keys

- F6 — Focus, self-targeted.
- F7 — Bolt, targets the living entity under the crosshair.
- F8 — Channel, targets the living entity under the crosshair.

These bindings are developer fixtures.

They are **not** the final action bar, class controls, spellbook, targeting
model, or default key layout.

## End-to-end path

A key press now travels:

```text
client developer key
-> crosshair/self target UUID
-> C2S activation payload
-> server sequence/replay guard
-> server entity resolution
-> entity-backed CombatActor state
-> fresh target observation
-> server CastController
-> resource/cooldown/condition validation
-> cast/effect mutation
-> S2C structured activation response
-> developer feedback
```

No gameplay result is calculated by the client.

## Why this surface is intentionally disposable

The final game needs:

- target selection beyond vanilla crosshair reach,
- action bars,
- key rebinding and bar paging,
- cast bars,
- cooldown presentation,
- target frames,
- failure feedback localization,
- ability tooltips,
- possibly tab/soft targeting.

Building those now would confuse P3 mechanism proof with P7 client production.

The developer keys exist to prove the transport and server runtime before that
larger UI work begins.
