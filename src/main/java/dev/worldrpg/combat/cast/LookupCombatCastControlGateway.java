package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.actor.CombatActorId;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.event.CombatEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Cast-control gateway backed by an injected actor -> controller lookup.
 */
public final class LookupCombatCastControlGateway
        implements CombatCastControlGateway {
    private static final RpgId MISSING_CONTROLLER =
            RpgId.parse(
                    "world_rpg:condition/missing_cast_controller"
            );
    private static final RpgId TARGET_NOT_CASTING =
            RpgId.parse(
                    "world_rpg:condition/target_not_casting"
            );
    private static final RpgId CAST_UNINTERRUPTIBLE =
            RpgId.parse(
                    "world_rpg:condition/cast_uninterruptible"
            );

    private final Function<CombatActorId, Optional<CastController>>
            lookup;

    public LookupCombatCastControlGateway(
            Function<CombatActorId, Optional<CastController>> lookup
    ) {
        this.lookup = Objects.requireNonNull(lookup, "lookup");
    }

    @Override
    public ConditionResult validateInterrupt(
            CastInterruptionRequest request
    ) {
        Objects.requireNonNull(request, "request");

        Optional<CastController> controller =
                lookup.apply(request.target().id());

        if (controller.isEmpty()) {
            return ConditionResult.fail(
                    MISSING_CONTROLLER,
                    "No cast controller for target "
                            + request.target().id()
            );
        }

        Optional<ActiveCast> active =
                controller.get().activeCast();

        if (active.isEmpty()) {
            return ConditionResult.fail(
                    TARGET_NOT_CASTING,
                    "Target is not casting"
            );
        }

        if (request.reason() == CastInterruptionReason.INTERRUPT
                && active.get()
                        .ability()
                        .interruptionPolicy()
                        == dev.worldrpg.combat.ability.AbilityInterruptionPolicy.UNINTERRUPTIBLE) {
            return ConditionResult.fail(
                    CAST_UNINTERRUPTIBLE,
                    "Active cast is uninterruptible: "
                            + active.get().ability().id()
            );
        }

        return ConditionResult.pass();
    }

    @Override
    public List<CombatEvent> interrupt(
            CastInterruptionRequest request
    ) {
        ConditionResult validation =
                validateInterrupt(request);

        if (!validation.passed()) {
            throw new IllegalStateException(
                    "validated cast interruption became invalid: "
                            + validation.failures()
            );
        }

        CastController controller =
                lookup.apply(request.target().id())
                        .orElseThrow();

        return controller.interrupt(
                        request.reason(),
                        request.gameTick()
                )
                .<List<CombatEvent>>map(List::of)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "validated target had no active cast"
                        )
                );
    }
}
