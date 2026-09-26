# P4 healer resource-stress calibration

Status: FIRST HEALER / SUSTAINED RESOURCE SCENARIO

This scenario isolates healing-resource pressure without requiring threat or a
full party-role simulator.

## Structure

A level-20 reference healer protects a durable ally for a fixed 250-second
pressure window.

A hostile actor continuously attacks the ally.

The healer has one production heal:

- 2.5-second cast;
- 10 mana;
- base healing 25;
- Healing Power coefficient 0.60;
- 5% crit from the healer;
- legal only while the target is at or below 70% health.

The health threshold is implemented by the reusable production
ResourceConditions.targetAtOrBelowFraction condition.

PriorityAbilityPolicy therefore selects the heal only when CastController says
all of these are simultaneously true:

- the healer is not already casting;
- the ally is alive;
- the healer is alive;
- ally health is at/below the threshold;
- enough mana exists;
- the effect preflight is valid.

## Pressure source

The ally has:

- twice reference health;
- 1.5 times reference same-level armor.

The pressure attacker repeatedly uses a 2.5-second Physical attack with:

- base magnitude 6;
- Attack Power coefficient 0.40;
- 4% crit;
- normal direct-weapon miss semantics.

## Acceptance neighborhood

Across 256 deterministic seeds:

- average survival window remains roughly 240-250 seconds;
- healer spends almost the entire reference mana pool;
- substantial healing is actually applied;
- ally death remains possible but should stay in the low single/double-digit
  neighborhood rather than becoming certain;
- no explicit mana recovery occurs.

A representative seed must finish with the healer effectively out of mana
while the ally remains alive under meaningful health pressure.

## Design meaning

This proves that resource attrition is not only a damage-caster concept.

The same production resource/cast/math stack can express a healer who must
decide when healing is legal, spend a finite long-horizon mana pool, and
eventually reach a real exhaustion state without the simulator granting an
encounter reset.

## Still outside this scenario

- multiple heal ranks;
- emergency heal priority;
- mana regeneration;
- consumables;
- dispels;
- group threat;
- tank active mitigation;
- boss burst windows;
- healer movement pressure.

Those should layer onto this resource-stress baseline later.
