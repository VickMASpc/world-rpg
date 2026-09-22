package dev.worldrpg.combat.cast;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityContext;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityDefinition;
import dev.worldrpg.combat.actor.CombatActor;
import dev.worldrpg.combat.condition.ConditionResult;
import dev.worldrpg.combat.cooldown.CooldownBook;
import dev.worldrpg.combat.cooldown.CooldownKey;
import dev.worldrpg.combat.effect.EffectContext;
import dev.worldrpg.combat.event.AbilityActivatedEvent;
import dev.worldrpg.combat.event.CastCompletedEvent;
import dev.worldrpg.combat.event.CastInterruptedEvent;
import dev.worldrpg.combat.event.CastStartedEvent;
import dev.worldrpg.combat.event.CombatEvent;
import dev.worldrpg.combat.event.CooldownStartedEvent;
import dev.worldrpg.combat.event.ResourceChangedEvent;
import dev.worldrpg.combat.resource.ResourceChange;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.resource.ResourcePool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Server-side cast/cooldown controller for one combat actor.
 */
public final class CastController {
    private static final RpgId ALREADY_CASTING =
            RpgId.parse("world_rpg:condition/already_casting");
    private static final RpgId ABILITY_ON_COOLDOWN =
            RpgId.parse("world_rpg:condition/ability_on_cooldown");
    private static final RpgId GLOBAL_COOLDOWN =
            RpgId.parse("world_rpg:condition/global_cooldown");
    private static final RpgId MISSING_RESOURCE =
            RpgId.parse("world_rpg:condition/missing_resource_pool");
    private static final RpgId INSUFFICIENT_RESOURCE =
            RpgId.parse("world_rpg:condition/insufficient_resource");

    private final CombatActor owner;
    private final CooldownBook cooldowns;
    private ActiveCast activeCast;
    private long nextCastId = 1L;

    public CastController(CombatActor owner) {
        this(owner, new CooldownBook());
    }

    public CastController(CombatActor owner, CooldownBook cooldowns) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.cooldowns = Objects.requireNonNull(cooldowns, "cooldowns");
    }

    public CombatActor owner() {
        return owner;
    }

    public CooldownBook cooldowns() {
        return cooldowns;
    }

    public Optional<ActiveCast> activeCast() {
        return Optional.ofNullable(activeCast);
    }

    public AbilityActivationResult tryActivate(
            AbilityDefinition ability,
            CombatActor target,
            long gameTick
    ) {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(target, "target");
        requireTick(gameTick);

        AbilityContext abilityContext = new AbilityContext(owner, target, gameTick);
        EffectContext effectContext = new EffectContext(owner, target, gameTick);

        ConditionResult validation = ConditionResult.pass();

        if (activeCast != null) {
            validation = validation.plus(ConditionResult.fail(
                    ALREADY_CASTING,
                    "Actor is already casting " + activeCast.ability().id()
            ));
        }

        CooldownKey abilityCooldown = new CooldownKey(ability.id());

        if (!cooldowns.isReady(abilityCooldown, gameTick)) {
            validation = validation.plus(ConditionResult.fail(
                    ABILITY_ON_COOLDOWN,
                    "Ability is on cooldown for "
                            + cooldowns.remainingTicks(abilityCooldown, gameTick)
                            + " more ticks"
            ));
        }

        if (!cooldowns.isReady(CooldownBook.GLOBAL, gameTick)) {
            validation = validation.plus(ConditionResult.fail(
                    GLOBAL_COOLDOWN,
                    "Global cooldown has "
                            + cooldowns.remainingTicks(CooldownBook.GLOBAL, gameTick)
                            + " ticks remaining"
            ));
        }

        validation = validation.plus(ability.activationCondition().evaluate(abilityContext));
        validation = validation.plus(ability.effects().validate(effectContext));

        Map<ResourceKey, Double> aggregatedCosts = aggregateCosts(ability.costs());
        validation = validation.plus(validateCosts(aggregatedCosts));

        if (!validation.passed()) {
            return new AbilityActivationResult(
                    validation,
                    Optional.empty(),
                    List.of()
            );
        }

        List<CombatEvent> events = new ArrayList<>();
        events.add(new AbilityActivatedEvent(
                gameTick,
                owner.id(),
                target.id(),
                ability.id()
        ));

        spendCosts(aggregatedCosts, gameTick, events);
        startCooldowns(ability, gameTick, events);

        if (ability.castKind() == AbilityCastKind.INSTANT) {
            events.addAll(ability.effects().applyValidated(effectContext));
            return new AbilityActivationResult(
                    ConditionResult.pass(),
                    Optional.empty(),
                    events
            );
        }

        CastId castId = new CastId(nextCastId++);
        long endsAtTick = safeAdd(gameTick, ability.castDurationTicks());
        OptionalLong nextChannelTick = ability.castKind() == AbilityCastKind.CHANNEL
                ? OptionalLong.of(safeAdd(
                        gameTick,
                        ability.channelIntervalTicks().orElseThrow()
                ))
                : OptionalLong.empty();

        activeCast = new ActiveCast(
                castId,
                ability,
                owner,
                target,
                gameTick,
                endsAtTick,
                nextChannelTick
        );

        events.add(new CastStartedEvent(
                gameTick,
                castId,
                owner.id(),
                target.id(),
                ability.id(),
                ability.castKind(),
                endsAtTick
        ));

        return new AbilityActivationResult(
                ConditionResult.pass(),
                Optional.of(castId),
                events
        );
    }

    public List<CombatEvent> tick(long gameTick) {
        requireTick(gameTick);

        if (activeCast == null) {
            return List.of();
        }

        return switch (activeCast.ability().castKind()) {
            case INSTANT -> throw new IllegalStateException(
                    "instant ability cannot own an active cast"
            );
            case TIMED -> tickTimed(gameTick);
            case CHANNEL -> tickChannel(gameTick);
        };
    }

    public Optional<CastInterruptedEvent> interrupt(
            CastInterruptionReason reason,
            long gameTick
    ) {
        Objects.requireNonNull(reason, "reason");
        requireTick(gameTick);

        if (activeCast == null) {
            return Optional.empty();
        }

        ActiveCast interrupted = activeCast;
        activeCast = null;

        return Optional.of(new CastInterruptedEvent(
                gameTick,
                interrupted.id(),
                owner.id(),
                interrupted.ability().id(),
                reason
        ));
    }

    private List<CombatEvent> tickTimed(long gameTick) {
        if (gameTick < activeCast.endsAtTick()) {
            return List.of();
        }

        if (!stillValid(activeCast, gameTick)) {
            return List.of(interrupt(
                    CastInterruptionReason.TARGET_INVALID,
                    gameTick
            ).orElseThrow());
        }

        ActiveCast completed = activeCast;
        activeCast = null;

        List<CombatEvent> events = new ArrayList<>();
        events.add(new CastCompletedEvent(
                gameTick,
                completed.id(),
                owner.id(),
                completed.target().id(),
                completed.ability().id()
        ));
        events.addAll(completed.ability().effects().applyValidated(
                new EffectContext(owner, completed.target(), gameTick)
        ));

        return List.copyOf(events);
    }

    private List<CombatEvent> tickChannel(long gameTick) {
        ActiveCast channel = activeCast;
        List<CombatEvent> events = new ArrayList<>();

        while (channel.nextChannelTick().isPresent()
                && channel.nextChannelTick().getAsLong() <= gameTick
                && channel.nextChannelTick().getAsLong() <= channel.endsAtTick()) {
            long scheduledTick = channel.nextChannelTick().getAsLong();

            if (!stillValid(channel, scheduledTick)) {
                events.add(interrupt(
                        CastInterruptionReason.TARGET_INVALID,
                        scheduledTick
                ).orElseThrow());
                return List.copyOf(events);
            }

            events.addAll(channel.ability().effects().applyValidated(
                    new EffectContext(owner, channel.target(), scheduledTick)
            ));

            long next = safeAdd(
                    scheduledTick,
                    channel.ability().channelIntervalTicks().orElseThrow()
            );
            channel.setNextChannelTick(OptionalLong.of(next));
        }

        if (activeCast != null && gameTick >= channel.endsAtTick()) {
            activeCast = null;
            events.add(new CastCompletedEvent(
                    gameTick,
                    channel.id(),
                    owner.id(),
                    channel.target().id(),
                    channel.ability().id()
            ));
        }

        return List.copyOf(events);
    }

    private boolean stillValid(ActiveCast cast, long gameTick) {
        AbilityContext abilityContext =
                new AbilityContext(owner, cast.target(), gameTick);
        EffectContext effectContext =
                new EffectContext(owner, cast.target(), gameTick);

        return cast.ability().activationCondition().evaluate(abilityContext).passed()
                && cast.ability().effects().validate(effectContext).passed();
    }

    private ConditionResult validateCosts(Map<ResourceKey, Double> costs) {
        ConditionResult result = ConditionResult.pass();

        for (Map.Entry<ResourceKey, Double> entry : costs.entrySet()) {
            Optional<ResourcePool> pool = owner.resources().find(entry.getKey());

            if (pool.isEmpty()) {
                result = result.plus(ConditionResult.fail(
                        MISSING_RESOURCE,
                        "Actor does not have resource pool " + entry.getKey()
                ));
                continue;
            }

            if (pool.get().current() < entry.getValue()) {
                result = result.plus(ConditionResult.fail(
                        INSUFFICIENT_RESOURCE,
                        "Need " + entry.getValue() + " " + entry.getKey()
                                + " but only have " + pool.get().current()
                ));
            }
        }

        return result;
    }

    private void spendCosts(
            Map<ResourceKey, Double> costs,
            long gameTick,
            List<CombatEvent> events
    ) {
        for (Map.Entry<ResourceKey, Double> entry : costs.entrySet()) {
            ResourcePool pool = owner.resources().require(entry.getKey());
            ResourceChange change = pool.spend(entry.getValue());

            if (!change.fullyApplied()) {
                throw new IllegalStateException(
                        "validated ability cost failed during spend: " + entry.getKey()
                );
            }

            events.add(new ResourceChangedEvent(
                    gameTick,
                    owner.id(),
                    entry.getKey(),
                    change
            ));
        }
    }

    private void startCooldowns(
            AbilityDefinition ability,
            long gameTick,
            List<CombatEvent> events
    ) {
        if (ability.cooldownTicks() > 0) {
            CooldownKey key = new CooldownKey(ability.id());
            long readyAt = cooldowns.start(key, ability.cooldownTicks(), gameTick);

            events.add(new CooldownStartedEvent(
                    gameTick,
                    owner.id(),
                    key,
                    readyAt
            ));
        }

        if (ability.globalCooldownTicks() > 0) {
            long readyAt = cooldowns.start(
                    CooldownBook.GLOBAL,
                    ability.globalCooldownTicks(),
                    gameTick
            );

            events.add(new CooldownStartedEvent(
                    gameTick,
                    owner.id(),
                    CooldownBook.GLOBAL,
                    readyAt
            ));
        }
    }

    private static Map<ResourceKey, Double> aggregateCosts(List<AbilityCost> costs) {
        Map<ResourceKey, Double> aggregated = new LinkedHashMap<>();

        for (AbilityCost cost : costs) {
            aggregated.merge(cost.resource(), cost.amount(), Double::sum);

            double total = aggregated.get(cost.resource());
            if (!Double.isFinite(total)) {
                throw new IllegalArgumentException(
                        "aggregated ability cost is not finite for " + cost.resource()
                );
            }
        }

        return aggregated;
    }

    private static long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static void requireTick(long gameTick) {
        if (gameTick < 0) {
            throw new IllegalArgumentException("gameTick must be >= 0");
        }
    }
}
