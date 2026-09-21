# Classes and abilities

Status: DRAFT

Do not create one Java class per spell. Abilities are definitions composed from targeting, requirements, cast behavior, costs, cooldowns, conditions, effects and presentation hooks.

Expected cast forms: instant, timed cast, channel, optional charge, toggle, on-next-action and passive/triggered.

Initial effect primitives to design: damage, heal, apply/remove aura, dispel, interrupt, resource change, threat change, movement/knockback, summon, teleport, execute and secondary ability trigger.

Conditions should cover target relation/state, resources, equipment, class/talent, position/facing, movement, aura presence, health thresholds, environment/zone, cooldown/charge and pet state.

Exact class roster is not frozen. The engine must not hard-code assumptions that only fit the first classes.
