package dev.worldrpg.combat.reaction;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.event.CombatEventEnvelope;

import java.util.List;

/**
 * Pure event observer contract.
 *
 * <p>Implementations must not mutate combat state from react(). They return
 * reaction requests which the dispatcher executes through the configured
 * executor.</p>
 */
public interface CombatEventListener {
    RpgId id();

    /**
     * Lower priorities run first. Equal priorities preserve registration order.
     */
    int priority();

    boolean supports(CombatEventEnvelope event);

    List<CombatReaction> react(CombatEventEnvelope event);
}
