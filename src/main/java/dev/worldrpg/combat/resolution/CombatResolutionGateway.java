package dev.worldrpg.combat.resolution;

import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;

/**
 * Boundary implemented by P4 mathematics.
 *
 * <p>P3 deliberately provides no default implementation. A caller must use an
 * explicit P4 resolver before production damage/healing can occur.</p>
 */
@FunctionalInterface
public interface CombatResolutionGateway {
    /**
     * Resolves one server-owned request and returns the ordered gameplay events
     * produced by that resolution.
     *
     * <p>The implementation owns any authoritative state mutation associated
     * with the outcome. Presentation/client code must never implement this
     * gateway.</p>
     */
    List<CombatEvent> resolve(CombatMagnitudeRequest request);
}
