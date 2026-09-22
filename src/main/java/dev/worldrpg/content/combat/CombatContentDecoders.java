package dev.worldrpg.content.combat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.combat.ability.AbilityCastKind;
import dev.worldrpg.combat.ability.AbilityCost;
import dev.worldrpg.combat.ability.AbilityMovementPolicy;
import dev.worldrpg.combat.aura.AuraRefreshPolicy;
import dev.worldrpg.combat.aura.AuraStatModifier;
import dev.worldrpg.combat.aura.AuraUniqueness;
import dev.worldrpg.combat.effect.EffectRecipient;
import dev.worldrpg.combat.resource.ResourceKey;
import dev.worldrpg.combat.stat.StatKey;
import dev.worldrpg.combat.stat.StatModifierOperation;
import dev.worldrpg.content.decode.DecodedJsonDocument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

final class CombatContentDecoders {
    private CombatContentDecoders() {
    }

    static Optional<AuraContentDefinition> decodeAura(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();

        Optional<Integer> maxStacks =
                requiredInt(root, "max_stacks", document, report);
        Optional<OptionalLong> duration = optionalLong(
                root,
                "duration_ticks",
                document,
                report
        );
        Optional<AuraUniqueness> uniqueness = requiredEnum(
                root,
                "uniqueness",
                AuraUniqueness.class,
                document,
                report
        );
        Optional<AuraRefreshPolicy> refresh = requiredEnum(
                root,
                "refresh_policy",
                AuraRefreshPolicy.class,
                document,
                report
        );
        Optional<List<AuraStatModifier>> modifiers =
                decodeStatModifiers(root, document, report);

        if (maxStacks.isEmpty()
                || duration.isEmpty()
                || uniqueness.isEmpty()
                || refresh.isEmpty()
                || modifiers.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new AuraContentDefinition(
                    document.header().id(),
                    maxStacks.get(),
                    duration.get(),
                    uniqueness.get(),
                    refresh.get(),
                    modifiers.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("aura.invalid", exception, document, report);
        }
    }

    static Optional<AbilityContentDefinition> decodeAbility(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();

        Optional<AbilityCastKind> castKind = requiredEnum(
                root,
                "cast_kind",
                AbilityCastKind.class,
                document,
                report
        );
        Optional<Long> castDuration =
                requiredLong(root, "cast_duration_ticks", document, report);
        Optional<OptionalLong> channelInterval = optionalLong(
                root,
                "channel_interval_ticks",
                document,
                report
        );
        Optional<Long> cooldown =
                requiredLong(root, "cooldown_ticks", document, report);
        Optional<Long> globalCooldown =
                requiredLong(root, "global_cooldown_ticks", document, report);
        Optional<AbilityMovementPolicy> movement = requiredEnum(
                root,
                "movement_policy",
                AbilityMovementPolicy.class,
                document,
                report
        );
        Optional<List<AbilityCost>> costs =
                decodeCosts(root, document, report);
        Optional<List<AbilityConditionSpec>> conditions =
                decodeConditions(root, document, report);
        Optional<List<AbilityEffectSpec>> effects =
                decodeEffects(root, document, report);

        if (castKind.isEmpty()
                || castDuration.isEmpty()
                || channelInterval.isEmpty()
                || cooldown.isEmpty()
                || globalCooldown.isEmpty()
                || movement.isEmpty()
                || costs.isEmpty()
                || conditions.isEmpty()
                || effects.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new AbilityContentDefinition(
                    document.header().id(),
                    castKind.get(),
                    castDuration.get(),
                    channelInterval.get(),
                    cooldown.get(),
                    globalCooldown.get(),
                    movement.get(),
                    costs.get(),
                    conditions.get(),
                    effects.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("ability.invalid", exception, document, report);
        }
    }

    private static Optional<List<AuraStatModifier>> decodeStatModifiers(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "stat_modifiers", document, report);
        if (array == null) {
            return Optional.empty();
        }

        List<AuraStatModifier> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = objectAt(array, i, "aura.stat_modifiers", document, report);
            if (object == null) {
                return Optional.empty();
            }

            Optional<RpgId> stat = requiredId(object, "stat", document, report);
            Optional<StatModifierOperation> operation = requiredEnum(
                    object,
                    "operation",
                    StatModifierOperation.class,
                    document,
                    report
            );
            Optional<Double> amount = requiredDouble(
                    object,
                    "amount_per_stack",
                    document,
                    report
            );
            Optional<Integer> priority =
                    requiredInt(object, "priority", document, report);

            if (stat.isEmpty()
                    || operation.isEmpty()
                    || amount.isEmpty()
                    || priority.isEmpty()) {
                return Optional.empty();
            }

            try {
                result.add(new AuraStatModifier(
                        new StatKey(stat.get()),
                        operation.get(),
                        amount.get(),
                        priority.get()
                ));
            } catch (IllegalArgumentException exception) {
                return invalidList(
                        "aura.stat_modifier.invalid",
                        exception,
                        document,
                        report
                );
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<List<AbilityCost>> decodeCosts(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "costs", document, report);
        if (array == null) {
            return Optional.empty();
        }

        List<AbilityCost> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = objectAt(array, i, "ability.costs", document, report);
            if (object == null) {
                return Optional.empty();
            }

            Optional<RpgId> resource =
                    requiredId(object, "resource", document, report);
            Optional<Double> amount =
                    requiredDouble(object, "amount", document, report);

            if (resource.isEmpty() || amount.isEmpty()) {
                return Optional.empty();
            }

            try {
                result.add(new AbilityCost(
                        new ResourceKey(resource.get()),
                        amount.get()
                ));
            } catch (IllegalArgumentException exception) {
                return invalidList(
                        "ability.cost.invalid",
                        exception,
                        document,
                        report
                );
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<List<AbilityConditionSpec>> decodeConditions(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "conditions", document, report);
        if (array == null) {
            return Optional.empty();
        }

        List<AbilityConditionSpec> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = objectAt(
                    array,
                    i,
                    "ability.conditions",
                    document,
                    report
            );
            if (object == null) {
                return Optional.empty();
            }

            Optional<String> type =
                    requiredString(object, "type", document, report);
            if (type.isEmpty()) {
                return Optional.empty();
            }

            try {
                switch (type.get()) {
                    case "source_alive" ->
                            result.add(new AbilityConditionSpec.SourceAlive());
                    case "target_alive" ->
                            result.add(new AbilityConditionSpec.TargetAlive());
                    case "same_world" ->
                            result.add(new AbilityConditionSpec.SameWorld());
                    case "disallow_self" ->
                            result.add(new AbilityConditionSpec.DisallowSelf());
                    case "require_self" ->
                            result.add(new AbilityConditionSpec.RequireSelf());
                    case "line_of_sight" ->
                            result.add(new AbilityConditionSpec.LineOfSight());
                    case "max_range" -> {
                        Optional<Double> blocks = requiredDouble(
                                object,
                                "blocks",
                                document,
                                report
                        );
                        if (blocks.isEmpty()) return Optional.empty();
                        result.add(new AbilityConditionSpec.MaxRange(blocks.get()));
                    }
                    case "facing_arc" -> {
                        Optional<Double> degrees = requiredDouble(
                                object,
                                "degrees",
                                document,
                                report
                        );
                        if (degrees.isEmpty()) return Optional.empty();
                        result.add(new AbilityConditionSpec.FacingArc(degrees.get()));
                    }
                    case "target_resource_at_least" -> {
                        Optional<RpgId> resource = requiredId(
                                object,
                                "resource",
                                document,
                                report
                        );
                        Optional<Double> amount = requiredDouble(
                                object,
                                "amount",
                                document,
                                report
                        );
                        if (resource.isEmpty() || amount.isEmpty()) {
                            return Optional.empty();
                        }
                        result.add(
                                new AbilityConditionSpec.TargetResourceAtLeast(
                                        new ResourceKey(resource.get()),
                                        amount.get()
                                )
                        );
                    }
                    default -> {
                        report.error(
                                "ability.condition.unknown_type",
                                "Unknown ability condition type: " + type.get(),
                                document.source().sourceRef(),
                                document.header().id()
                        );
                        return Optional.empty();
                    }
                }
            } catch (IllegalArgumentException exception) {
                return invalidList(
                        "ability.condition.invalid",
                        exception,
                        document,
                        report
                );
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<List<AbilityEffectSpec>> decodeEffects(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "effects", document, report);
        if (array == null) {
            return Optional.empty();
        }

        List<AbilityEffectSpec> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = objectAt(
                    array,
                    i,
                    "ability.effects",
                    document,
                    report
            );
            if (object == null) {
                return Optional.empty();
            }

            Optional<String> type =
                    requiredString(object, "type", document, report);
            Optional<EffectRecipient> recipient = requiredEnum(
                    object,
                    "recipient",
                    EffectRecipient.class,
                    document,
                    report
            );

            if (type.isEmpty() || recipient.isEmpty()) {
                return Optional.empty();
            }

            try {
                switch (type.get()) {
                    case "drain_resource" -> {
                        Optional<RpgId> resource = requiredId(
                                object,
                                "resource",
                                document,
                                report
                        );
                        Optional<Double> amount = requiredDouble(
                                object,
                                "amount",
                                document,
                                report
                        );
                        if (resource.isEmpty() || amount.isEmpty()) {
                            return Optional.empty();
                        }

                        result.add(new AbilityEffectSpec.ResourceDrain(
                                recipient.get(),
                                new ResourceKey(resource.get()),
                                amount.get()
                        ));
                    }
                    case "apply_aura" -> {
                        Optional<RpgId> aura = requiredId(
                                object,
                                "aura",
                                document,
                                report
                        );
                        if (aura.isEmpty()) return Optional.empty();

                        result.add(new AbilityEffectSpec.ApplyAura(
                                recipient.get(),
                                new RequiredDefinitionRef<>(
                                        CombatContentDomains.AURAS,
                                        aura.get()
                                )
                        ));
                    }
                    default -> {
                        report.error(
                                "ability.effect.unknown_type",
                                "Unknown ability effect type: " + type.get(),
                                document.source().sourceRef(),
                                document.header().id()
                        );
                        return Optional.empty();
                    }
                }
            } catch (IllegalArgumentException exception) {
                return invalidList(
                        "ability.effect.invalid",
                        exception,
                        document,
                        report
                );
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static JsonArray arrayOrEmpty(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);
        if (element == null) return new JsonArray();

        if (!element.isJsonArray()) {
            report.error(
                    "combat.field.array",
                    "Field '" + field + "' must be an array",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return null;
        }

        return element.getAsJsonArray();
    }

    private static JsonObject objectAt(
            JsonArray array,
            int index,
            String label,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = array.get(index);

        if (!element.isJsonObject()) {
            report.error(
                    "combat.array.object",
                    label + "[" + index + "] must be an object",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return null;
        }

        return element.getAsJsonObject();
    }

    private static Optional<String> requiredString(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);

        if (element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()) {
            report.error(
                    "combat.field.string",
                    "Field '" + field + "' must be a string",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }

        return Optional.of(element.getAsString());
    }

    private static Optional<RpgId> requiredId(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        Optional<String> text =
                requiredString(object, field, document, report);
        if (text.isEmpty()) return Optional.empty();

        try {
            return Optional.of(RpgId.parse(text.get()));
        } catch (IllegalArgumentException exception) {
            report.error(
                    "combat.field.id",
                    "Field '" + field + "': " + exception.getMessage(),
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<Integer> requiredInt(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);

        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new ArithmeticException();
            }

            BigDecimal decimal =
                    element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) throw new ArithmeticException();

            return Optional.of(decimal.intValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "combat.field.integer",
                    "Field '" + field + "' must be an exact integer",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<Long> requiredLong(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);

        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new ArithmeticException();
            }

            BigDecimal decimal =
                    element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) throw new ArithmeticException();

            return Optional.of(decimal.longValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "combat.field.long",
                    "Field '" + field + "' must be an exact integer",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<OptionalLong> optionalLong(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);
        if (element == null || element.isJsonNull()) {
            return Optional.of(OptionalLong.empty());
        }

        Optional<Long> value =
                requiredLong(object, field, document, report);

        return value.map(number ->
                OptionalLong.of(number)
        );
    }

    private static Optional<Double> requiredDouble(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = object.get(field);

        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException();
            }

            double value = element.getAsDouble();
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException();
            }

            return Optional.of(value);
        } catch (RuntimeException exception) {
            report.error(
                    "combat.field.number",
                    "Field '" + field + "' must be a finite number",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static <E extends Enum<E>> Optional<E> requiredEnum(
            JsonObject object,
            String field,
            Class<E> type,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        Optional<String> text =
                requiredString(object, field, document, report);
        if (text.isEmpty()) return Optional.empty();

        try {
            return Optional.of(Enum.valueOf(
                    type,
                    text.get().toUpperCase(Locale.ROOT)
            ));
        } catch (IllegalArgumentException exception) {
            report.error(
                    "combat.field.enum",
                    "Field '" + field + "' has unsupported value '"
                            + text.get() + "'",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static <T> Optional<T> invalid(
            String code,
            RuntimeException exception,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        report.error(
                code,
                exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage(),
                document.source().sourceRef(),
                document.header().id()
        );
        return Optional.empty();
    }

    private static <T> Optional<List<T>> invalidList(
            String code,
            RuntimeException exception,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        report.error(
                code,
                exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage(),
                document.source().sourceRef(),
                document.header().id()
        );
        return Optional.empty();
    }
}
