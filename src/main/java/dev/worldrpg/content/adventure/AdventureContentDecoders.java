package dev.worldrpg.content.adventure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.decode.DecodedJsonDocument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class AdventureContentDecoders {
    private AdventureContentDecoders() {
    }

    static Optional<WorldLocationContentDefinition> decodeLocation(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<String> displayName = requiredString(root, "display_name", document, report);
        Optional<String> kind = requiredString(root, "kind", document, report);
        Optional<List<String>> tags = stringArray(root, "tags", document, report);

        if (displayName.isEmpty() || kind.isEmpty() || tags.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new WorldLocationContentDefinition(
                    document.header().id(),
                    displayName.get(),
                    kind.get(),
                    tags.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("adventure.location.invalid", exception, document, report);
        }
    }

    static Optional<NpcContentDefinition> decodeNpc(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<String> displayName = requiredString(root, "display_name", document, report);
        Optional<RpgId> homeLocation = requiredId(root, "home_location", document, report);
        Optional<List<String>> roles = stringArray(root, "roles", document, report);

        if (displayName.isEmpty() || homeLocation.isEmpty() || roles.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new NpcContentDefinition(
                    document.header().id(),
                    displayName.get(),
                    new RequiredDefinitionRef<>(
                            AdventureContentDomains.WORLD_LOCATIONS,
                            homeLocation.get()
                    ),
                    roles.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("adventure.npc.invalid", exception, document, report);
        }
    }

    static Optional<ItemContentDefinition> decodeItem(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<String> displayName = requiredString(root, "display_name", document, report);
        Optional<ItemContentDefinition.Category> category = requiredEnum(
                root,
                "category",
                ItemContentDefinition.Category.class,
                document,
                report
        );
        Optional<Integer> requiredLevel = requiredInt(
                root,
                "required_level",
                document,
                report
        );
        Optional<Long> vendorValue = requiredLong(
                root,
                "vendor_value_copper",
                document,
                report
        );
        Optional<List<String>> tags = stringArray(root, "tags", document, report);

        if (displayName.isEmpty()
                || category.isEmpty()
                || requiredLevel.isEmpty()
                || vendorValue.isEmpty()
                || tags.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new ItemContentDefinition(
                    document.header().id(),
                    displayName.get(),
                    category.get(),
                    requiredLevel.get(),
                    vendorValue.get(),
                    tags.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("adventure.item.invalid", exception, document, report);
        }
    }

    static Optional<QuestContentDefinition> decodeQuest(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<String> title = requiredString(root, "title", document, report);
        Optional<String> journalSummary = requiredString(
                root,
                "journal_summary",
                document,
                report
        );
        Optional<Integer> minimumLevel = requiredInt(
                root,
                "minimum_level",
                document,
                report
        );
        Optional<RpgId> starter = requiredId(root, "starter", document, report);
        Optional<RpgId> turnIn = requiredId(root, "turn_in", document, report);
        Optional<List<QuestObjectiveSpec>> objectives = objectives(
                root,
                document,
                report
        );
        Optional<List<QuestItemRewardSpec>> itemRewards =
                itemRewards(root, document, report);
        Optional<Long> copperReward = requiredLong(
                root,
                "copper_reward",
                document,
                report
        );

        if (title.isEmpty()
                || journalSummary.isEmpty()
                || minimumLevel.isEmpty()
                || starter.isEmpty()
                || turnIn.isEmpty()
                || objectives.isEmpty()
                || itemRewards.isEmpty()
                || copperReward.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new QuestContentDefinition(
                    document.header().id(),
                    title.get(),
                    journalSummary.get(),
                    minimumLevel.get(),
                    new RequiredDefinitionRef<>(
                            AdventureContentDomains.NPCS,
                            starter.get()
                    ),
                    new RequiredDefinitionRef<>(
                            AdventureContentDomains.NPCS,
                            turnIn.get()
                    ),
                    objectives.get(),
                    itemRewards.get(),
                    copperReward.get()
            ));
        } catch (IllegalArgumentException exception) {
            return invalid("adventure.quest.invalid", exception, document, report);
        }
    }

    private static Optional<List<QuestObjectiveSpec>> objectives(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "objectives", document, report);
        if (array == null) return Optional.empty();

        List<QuestObjectiveSpec> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (!element.isJsonObject()) {
                report.error(
                        "adventure.quest.objective.object",
                        "Quest objective[" + i + "] must be an object",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }

            JsonObject object = element.getAsJsonObject();
            Optional<String> key = requiredString(object, "key", document, report);
            Optional<String> type = requiredString(object, "type", document, report);
            if (key.isEmpty() || type.isEmpty()) return Optional.empty();

            switch (type.get()) {
                case "visit_location" -> {
                    Optional<RpgId> location = requiredId(
                            object,
                            "location",
                            document,
                            report
                    );
                    if (location.isEmpty()) return Optional.empty();
                    result.add(new QuestObjectiveSpec.VisitLocation(
                            key.get(),
                            new RequiredDefinitionRef<>(
                                    AdventureContentDomains.WORLD_LOCATIONS,
                                    location.get()
                            )
                    ));
                }
                case "speak_to_npc" -> {
                    Optional<RpgId> npc = requiredId(object, "npc", document, report);
                    if (npc.isEmpty()) return Optional.empty();
                    result.add(new QuestObjectiveSpec.SpeakToNpc(
                            key.get(),
                            new RequiredDefinitionRef<>(
                                    AdventureContentDomains.NPCS,
                                    npc.get()
                            )
                    ));
                }
                default -> {
                    report.error(
                            "adventure.quest.objective.unknown_type",
                            "Unknown quest objective type: " + type.get(),
                            document.source().sourceRef(),
                            document.header().id()
                    );
                    return Optional.empty();
                }
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<List<QuestItemRewardSpec>> itemRewards(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, "item_rewards", document, report);
        if (array == null) return Optional.empty();

        List<QuestItemRewardSpec> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (!element.isJsonObject()) {
                report.error(
                        "adventure.quest.item_reward.object",
                        "Quest item_rewards[" + i + "] must be an object",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }

            JsonObject object = element.getAsJsonObject();
            Optional<RpgId> item = requiredId(
                    object, "item", document, report
            );
            Optional<Integer> quantity = requiredInt(
                    object, "quantity", document, report
            );
            if (item.isEmpty() || quantity.isEmpty()) {
                return Optional.empty();
            }

            try {
                result.add(new QuestItemRewardSpec(
                        new RequiredDefinitionRef<>(
                                AdventureContentDomains.ITEMS,
                                item.get()
                        ),
                        quantity.get()
                ));
            } catch (IllegalArgumentException exception) {
                report.error(
                        "adventure.quest.item_reward.invalid",
                        exception.getMessage() == null
                                ? exception.getClass().getSimpleName()
                                : exception.getMessage(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<List<String>> stringArray(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonArray array = arrayOrEmpty(root, field, document, report);
        if (array == null) return Optional.empty();

        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (!element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isString()
                    || element.getAsString().trim().isEmpty()) {
                report.error(
                        "adventure.field.string_array",
                        "Field '" + field + "' must contain only non-blank strings",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }
            result.add(element.getAsString().trim());
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
                    "adventure.field.array",
                    "Field '" + field + "' must be an array",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return null;
        }

        return element.getAsJsonArray();
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
                || !element.getAsJsonPrimitive().isString()
                || element.getAsString().trim().isEmpty()) {
            report.error(
                    "adventure.field.string",
                    "Field '" + field + "' must be a non-blank string",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }

        return Optional.of(element.getAsString().trim());
    }

    private static Optional<RpgId> requiredId(
            JsonObject object,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        Optional<String> text = requiredString(object, field, document, report);
        if (text.isEmpty()) return Optional.empty();

        try {
            return Optional.of(RpgId.parse(text.get()));
        } catch (IllegalArgumentException exception) {
            report.error(
                    "adventure.field.id",
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
            BigDecimal decimal = element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) throw new ArithmeticException();
            return Optional.of(decimal.intValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "adventure.field.integer",
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
            BigDecimal decimal = element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) throw new ArithmeticException();
            return Optional.of(decimal.longValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "adventure.field.long",
                    "Field '" + field + "' must be an exact integer",
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
        Optional<String> text = requiredString(object, field, document, report);
        if (text.isEmpty()) return Optional.empty();

        try {
            return Optional.of(Enum.valueOf(
                    type,
                    text.get().toUpperCase(Locale.ROOT)
            ));
        } catch (IllegalArgumentException exception) {
            report.error(
                    "adventure.field.enum",
                    "Field '" + field + "' has unsupported value '" + text.get() + "'",
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
}
