# Cycle 2 Packet 1 — Physical Evidence

Date: 2026-09-26

Branch: `p4/math-simulator`

Status: **PHYSICALLY ACCEPTED**

The user completed the Packet 1 character/equipment authority suite and confirmed that everything passed.

Accepted physical behavior:

- persistent level / XP state;
- level-up across the provisional opening threshold;
- immediate live production-combat refresh after level-up;
- Road-Worn Cloak recognized as BACK equipment;
- cloak armor modifier applied through the production StatSheet;
- level-gated Ashwood-Carved Charm rejection at level 1;
- deterministic XP grant reaches level 2;
- level-2 HP/AP baseline updates without reconnect;
- Charm equips in NECK at level 2;
- +3 attack-power equipment modifier appears in live character stats;
- unequip removes the modifier without consuming the owned bag item;
- authored Wolf/Stalker kills grant their authored XP rewards;
- progression/equipment survive world reload;
- resetplayer clears persistent progression/equipment and immediately reconciles live combat state.

Packet 1 therefore proves that character progression/equipment are no longer isolated persistence data. They materially affect the production combat actor and survive the player lifecycle.

---

# Constitutional interpretation

This acceptance does **not** authorize automatic continuation into polished character/bag/equipment UI.

After Packet 1 acceptance, the project was re-read against the original ideation and constitution. The correction is:

> choose the next work from the missing lived experience, not from the next convenient subsystem dependency.

The highest-value open lived-experience proof is still P4-G G9:

**Can the first-province topology create the intended distance, route choice, quiet travel, learned shortcuts, refuge value and mental-map memory when physically walked?**

Cycle 2 Packet 2 is therefore paused while the first-province topology proof becomes executable and is physically measured.

No Packet 1 implementation is rolled back.
