# Golden Ashwood Wolf — Iteration 0.1 Acceptance

## Purpose

This iteration is deliberately narrower than the full Era 3 golden package.

It proves one new connected production strand:

```text
editable creature source
        ↓
authored mob definition
        ↓
authored loot-table definition
        ↓
physical Minecraft living entity
        ↓
persistent authored-mob binding
        ↓
death event
        ↓
authored RPG loot
        ↓
persistent RPG bag
```

The temporary physical/render shell is a vanilla Minecraft wolf.

That is intentional. Custom model rendering, animations, VFX/SFX and the production combat-authority bridge are separate acceptance work for the next iteration.

## Automated expectations

Normal CI must prove:

- the complete catalog now admits the mob and loot-table registries;
- the packaged golden mob -> loot -> item graph publishes;
- a missing loot reference rejects the candidate content snapshot;
- existing content-domain tests remain green;
- World RPG PersistentState can serialize and reconstruct arbitrary world-binding and player RPG payloads.

## Physical smoke test

Build and load a disposable test world with cheats.

Run:

```text
/worldrpg content status
/worldrpg golden status
```

Expected:

- content is published without errors;
- authored mob runtime is started;
- mob binding count is initially 0 unless a previously spawned golden mob survived a reload.

Spawn the prototype:

```text
/worldrpg golden ashwood spawn
```

Expected:

- one named `Ashwood Wolf [Lv. 3]` appears roughly 10 blocks ahead;
- the command reports the authored mob ID;
- report includes `renderShell=minecraft:wolf`;
- report includes source master `assets-source/creatures/ashwood_wolf.bbmodel`.

Run:

```text
/worldrpg golden status
```

Expected:

```text
authored mob runtime | bindings=1
```

Fight and kill the wolf using normal Minecraft damage for this iteration.

Expected death message:

```text
Ashwood Wolf defeated. RPG loot: Ashwood Fang x1, 6 copper
```

Then run:

```text
/rpgbag
/worldrpg golden status
```

Expected bag additions:

- Ashwood Fang x1;
- +6 copper relative to the player's pre-kill RPG balance.

Expected authored mob bindings after death:

```text
authored mob runtime | bindings=0
```

## Reload durability bundle

This test intentionally also advances the remaining adventure durability evidence.

### Mob binding durability

1. spawn a golden Ashwood Wolf;
2. confirm `bindings=1`;
3. fully exit the world;
4. reopen the same save;
5. run `/worldrpg golden status`.

Expected:

- binding remains 1;
- the persistent wolf is still physically present when its area is loaded.

Kill it after reload.

Expected:

- authored loot grants once;
- binding returns to 0;
- repeated world reload does not grant another reward for the dead entity.

### Adventure E1-E3 closeout

In the same post-reload session run:

```text
/worldrpg slice status
/rpgbag
```

For the previously completed two-quest acceptance world, expected:

- quest1 COMPLETED 2/2;
- quest2 COMPLETED 2/2;
- Warden available;
- Scout available;
- 2 NPC bindings;
- 3 location bindings;
- 95 copper plus any golden-mob copper earned after that point;
- Road-Worn Cloak x1;
- Roadside Provisions x2.

Right-click Warden once and Scout once.

Expected:

- one NPC response per physical click;
- no quest mutation;
- no duplicated quest reward.

If those hold, adventure E1, E2 and E3 can be closed.

## Explicit non-goals

Do not interpret a pass as proof of:

- final Ashwood Wolf visuals;
- final Ashwood Wolf balance;
- custom creature renderer;
- GeckoLib admission;
- animation integration;
- World RPG combat-authority ownership of all damage;
- production spawn ecology;
- final loot/economy balance;
- province-canon status for the golden package.

The prototype IDs remain under `world_rpg:dev/golden/...`.

## Next iteration after pass

The next narrow problem is the **asset/runtime presentation bridge**:

```text
ashwood_wolf.bbmodel
        ↓
reproducible runtime export
        ↓
custom creature rendering
        ↓
idle/walk/bite state mapping
```

Only after that visual/animation path works should the golden package add VFX/SFX and then reconnect combat authority.
