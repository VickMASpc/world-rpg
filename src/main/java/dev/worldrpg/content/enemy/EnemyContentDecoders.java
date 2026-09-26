package dev.worldrpg.content.enemy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.reference.RequiredDefinitionRef;
import dev.worldrpg.content.adventure.AdventureContentDomains;
import dev.worldrpg.content.decode.DecodedJsonDocument;
import dev.worldrpg.api.validation.ValidationReport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class EnemyContentDecoders {
    private EnemyContentDecoders() {
    }

    static Optional<LootTableContentDefinition> decodeLootTable(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<List<LootEntrySpec>> entries =
                lootEntries(root, document, report);
        Optional<Long> minimumCopper = requiredLong(
                root,
                "minimum_copper",
                document,
                report
        );
        Optional<Long> maximumCopper = requiredLong(
                root,
                "maximum_copper",
                document,
                report
        );

        if (entries.isEmpty()
                || minimumCopper.isEmpty()
                || maximumCopper.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    new LootTableContentDefinition(
                            document.header().id(),
                            entries.get(),
                            minimumCopper.get(),
                            maximumCopper.get()
                    )
            );
        } catch (IllegalArgumentException exception) {
            return invalid(
                    "enemy.loot.invalid",
                    exception,
                    document,
                    report
            );
        }
    }

    static Optional<MobContentDefinition> decodeMob(
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonObject root = document.root();
        Optional<String> displayName = requiredString(
                root,
                "display_name",
                document,
                report
        );
        Optional<Integer> level = requiredInt(
                root,
                "level",
                document,
                report
        );
        Optional<Double> maximumHealth = requiredDouble(
                root,
                "maximum_health",
                document,
                report
        );
        Optional<Double> attackDamage = requiredDouble(
                root,
                "attack_damage",
                document,
                report
        );
        Optional<Double> movementSpeed = requiredDouble(
                root,
                "movement_speed",
                document,
                report
        );
        Optional<RpgId> minecraftEntityType = requiredId(
                root,
                "minecraft_entity_type",
                document,
                report
        );
        Optional<RpgId> lootTable = requiredId(
                root,
                "loot_table",
                document,
                report
        );
        Optional<String> sourceAsset = requiredString(
                root,
                "source_asset",
                document,
                report
        );

        if (displayName.isEmpty()
                || level.isEmpty()
                || maximumHealth.isEmpty()
                || attackDamage.isEmpty()
                || movementSpeed.isEmpty()
                || minecraftEntityType.isEmpty()
                || lootTable.isEmpty()
                || sourceAsset.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    new MobContentDefinition(
                            document.header().id(),
                            displayName.get(),
                            level.get(),
                            maximumHealth.get(),
                            attackDamage.get(),
                            movementSpeed.get(),
                            minecraftEntityType.get(),
                            new RequiredDefinitionRef<>(
                                    EnemyContentDomains.LOOT_TABLES,
                                    lootTable.get()
                            ),
                            sourceAsset.get()
                    )
            );
        } catch (IllegalArgumentException exception) {
            return invalid(
                    "enemy.mob.invalid",
                    exception,
                    document,
                    report
            );
        }
    }

    private static Optional<List<LootEntrySpec>> lootEntries(
            JsonObject root,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = root.get("entries");
        if (element == null || !element.isJsonArray()) {
            report.error(
                    "enemy.loot.entries",
                    "Field 'entries' must be an array",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }

        JsonArray array = element.getAsJsonArray();
        List<LootEntrySpec> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonObject()) {
                report.error(
                        "enemy.loot.entry.object",
                        "Loot entry[" + i + "] must be an object",
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }

            JsonObject value = array.get(i).getAsJsonObject();
            Optional<RpgId> item = requiredId(
                    value,
                    "item",
                    document,
                    report
            );
            Optional<Integer> minimum = requiredInt(
                    value,
                    "minimum_quantity",
                    document,
                    report
            );
            Optional<Integer> maximum = requiredInt(
                    value,
                    "maximum_quantity",
                    document,
                    report
            );
            Optional<Double> chance = requiredDouble(
                    value,
                    "chance",
                    document,
                    report
            );
            if (item.isEmpty()
                    || minimum.isEmpty()
                    || maximum.isEmpty()
                    || chance.isEmpty()) {
                return Optional.empty();
            }

            try {
                result.add(new LootEntrySpec(
                        new RequiredDefinitionRef<>(
                                AdventureContentDomains.ITEMS,
                                item.get()
                        ),
                        minimum.get(),
                        maximum.get(),
                        chance.get()
                ));
            } catch (IllegalArgumentException exception) {
                report.error(
                        "enemy.loot.entry.invalid",
                        exception.getMessage(),
                        document.source().sourceRef(),
                        document.header().id()
                );
                return Optional.empty();
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private static Optional<String> requiredString(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = root.get(field);
        if (element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()
                || element.getAsString().trim().isEmpty()) {
            report.error(
                    "enemy.field.string",
                    "Field '" + field + "' must be a non-blank string",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
        return Optional.of(element.getAsString().trim());
    }

    private static Optional<RpgId> requiredId(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        Optional<String> text = requiredString(
                root,
                field,
                document,
                report
        );
        if (text.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(RpgId.parse(text.get()));
        } catch (IllegalArgumentException exception) {
            report.error(
                    "enemy.field.id",
                    "Field '" + field + "': "
                            + exception.getMessage(),
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<Integer> requiredInt(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = root.get(field);
        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new ArithmeticException();
            }
            BigDecimal decimal =
                    element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) {
                throw new ArithmeticException();
            }
            return Optional.of(decimal.intValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "enemy.field.integer",
                    "Field '" + field + "' must be an exact integer",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<Long> requiredLong(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = root.get(field);
        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new ArithmeticException();
            }
            BigDecimal decimal =
                    element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) {
                throw new ArithmeticException();
            }
            return Optional.of(decimal.longValueExact());
        } catch (RuntimeException exception) {
            report.error(
                    "enemy.field.long",
                    "Field '" + field + "' must be an exact integer",
                    document.source().sourceRef(),
                    document.header().id()
            );
            return Optional.empty();
        }
    }

    private static Optional<Double> requiredDouble(
            JsonObject root,
            String field,
            DecodedJsonDocument document,
            ValidationReport report
    ) {
        JsonElement element = root.get(field);
        try {
            if (element == null
                    || !element.isJsonPrimitive()
                    || !element.getAsJsonPrimitive().isNumber()) {
                throw new NumberFormatException();
            }
            double value = element.getAsDouble();
            if (!Double.isFinite(value)) {
                throw new NumberFormatException();
            }
            return Optional.of(value);
        } catch (RuntimeException exception) {
            report.error(
                    "enemy.field.number",
                    "Field '" + field + "' must be a finite number",
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
