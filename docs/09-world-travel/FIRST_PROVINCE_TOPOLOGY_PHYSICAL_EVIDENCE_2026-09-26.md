# First Province Topology — Physical Evidence

Date: 2026-09-26

Branch: `p4/math-simulator`

Candidate head physically exercised:

`16e4f674751c1981257ab0057eb58ecefeb3e530`

CI: #280 PASS

Status: **G9 PARTIAL — graybox/world instrumentation physically proven; journey timing and mental-map rows remain open**

---

# Test environment

The user tested in a fresh Superflat Overworld.

The first build attempt exposed a real long-distance world-query defect:

- topology spans nearly 10,000 blocks;
- distant chunks were not loaded before `getTopY()`;
- the height query returned an effective dimension-floor value around y=-65;
- the flatness preflight falsely rejected a perfectly flat Superflat world as uneven.

The builder/runtime were repaired to load/generate sampled distant chunks before reading their heightmaps.

The repaired candidate passed CI #280 and then physically built successfully.

---

# Successful physical build

Observed:

```text
province graybox built at origin -10,-60,-1
routes=56937
bridge=205
landmarks=191
```

The persisted topology origin was then readable:

```text
province topology | origin=-10,-1 nearest=HOME distance=0.33 blocks active=none
```

The one-block difference between player origin Y (-60) and node ground Y (-61) is expected:

- build origin records the player's standing block position;
- node output reports the surface block below the player.

---

# Physical node graph evidence

The physical world reported the full authored graph:

```text
HOME                = -10,-61,-1
WILD                = 690,-61,-1
CORRIDOR            = 1890,-61,-1
FORK                = 3610,-61,-1
DANGER              = 2750,-61,-1
REFUGE              = 5790,-61,-1
REGIONAL_SETTLEMENT = 3610,-61,3599
BEYOND              = 9790,-61,-1
WORKLAND            = 690,-61,799
LOCAL_RUIN          = 690,-61,-1601
UNFINISHED_SITE     = 3090,-61,999
```

This confirms that the disposable world proof is kilometer-scale rather than acceptance-slice scale.

---

# Authored journey graph visible in client

The client successfully listed:

```text
a-to-r1-local    HOME -> LOCAL_RUIN            path≈2300 blocks  target=6–12 min
a-to-f-safe      HOME -> REFUGE                path≈6971 blocks  target=20–30 min
a-to-g-safe      HOME -> REGIONAL_SETTLEMENT   path≈7991 blocks  target=25–40 min
g-to-a-learned   REGIONAL_SETTLEMENT -> HOME   path≈5335 blocks  relative target
f-to-h-outward   REFUGE -> BEYOND              path≈4000 blocks  target=15–30 min
d-to-e-danger    FORK -> DANGER                path≈860 blocks   relative target
```

---

# Nearest-node runtime evidence

The user physically moved east from HOME toward WILD.

Observed:

```text
HOME distance ~= 180.61 blocks
nearest=HOME
```

Later:

```text
nearest=WILD
distance ~= 7.98 blocks
```

This physically verifies:

- persisted origin -> absolute node-coordinate translation;
- live player-position measurement;
- nearest-node calculation;
- kilometer-scale coordinate handling after chunk loading;
- node status remains usable while traversing the graybox.

---

# What this does NOT yet prove

G9 remains open for the important lived-experience rows:

- actual HOME -> LOCAL_RUIN travel timing;
- actual HOME -> REFUGE travel timing;
- actual HOME -> REGIONAL_SETTLEMENT first journey;
- learned REGIONAL_SETTLEMENT -> HOME shortcut comparison;
- outward REFUGE -> BEYOND leg;
- DANGER approach/retreat;
- actual distance-walked / route-ratio evidence;
- wrong-turn/legibility evidence;
- quiet-space quality;
- whether A and G feel psychologically separated;
- whether F genuinely feels like remote support;
- whether D becomes a remembered decision point;
- mental-map reconstruction after repeated traversal;
- one mixed 60–180 minute expedition.

The next evidence should come from measured physical journeys, not more world-system expansion.
