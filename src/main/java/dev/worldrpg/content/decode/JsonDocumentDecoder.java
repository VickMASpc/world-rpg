package dev.worldrpg.content.decode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.worldrpg.api.data.SchemaVersion;
import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSource;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Decodes only the common JSON document envelope.
 *
 * <p>Domain-specific fields remain untouched until a domain decoder receives
 * a structurally valid document.</p>
 */
public final class JsonDocumentDecoder {
    private JsonDocumentDecoder() {
    }

    public static Optional<DecodedJsonDocument> decode(
            ContentSource source,
            ValidationReport report
    ) {
        final JsonElement parsed;
        try {
            parsed = JsonParser.parseString(source.content());
        } catch (JsonParseException exception) {
            report.error(
                    "json.syntax",
                    "Invalid JSON: " + exception.getMessage(),
                    source.sourceRef()
            );
            return Optional.empty();
        }

        if (!parsed.isJsonObject()) {
            report.error(
                    "json.root_type",
                    "Definition root must be a JSON object",
                    source.sourceRef()
            );
            return Optional.empty();
        }

        JsonObject root = parsed.getAsJsonObject();

        Optional<SchemaVersion> schema = decodeSchema(root, source, report);
        Optional<RpgId> registry = decodeRpgId(
                root,
                "registry",
                "json.registry",
                source,
                report
        );
        Optional<RpgId> id = decodeRpgId(
                root,
                "id",
                "json.id",
                source,
                report
        );

        if (schema.isEmpty() || registry.isEmpty() || id.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DecodedJsonDocument(
                new DefinitionHeader(schema.get(), registry.get(), id.get()),
                root,
                source
        ));
    }

    private static Optional<SchemaVersion> decodeSchema(
            JsonObject root,
            ContentSource source,
            ValidationReport report
    ) {
        JsonElement element = root.get("schema");
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            report.error(
                    "json.schema.required_integer",
                    "Definition field 'schema' must be an integer >= 1",
                    source.sourceRef()
            );
            return Optional.empty();
        }

        try {
            BigDecimal decimal = element.getAsBigDecimal().stripTrailingZeros();
            if (decimal.scale() > 0) {
                throw new ArithmeticException("not an integer");
            }

            int value = decimal.intValueExact();
            return Optional.of(new SchemaVersion(value));
        } catch (ArithmeticException | IllegalArgumentException exception) {
            report.error(
                    "json.schema.required_integer",
                    "Definition field 'schema' must be an integer >= 1",
                    source.sourceRef()
            );
            return Optional.empty();
        }
    }

    private static Optional<RpgId> decodeRpgId(
            JsonObject root,
            String field,
            String codePrefix,
            ContentSource source,
            ValidationReport report
    ) {
        JsonElement element = root.get(field);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            report.error(
                    codePrefix + ".required_string",
                    "Definition field '" + field + "' must be a stable namespaced string",
                    source.sourceRef()
            );
            return Optional.empty();
        }

        try {
            return Optional.of(RpgId.parse(element.getAsString()));
        } catch (IllegalArgumentException exception) {
            report.error(
                    codePrefix + ".invalid",
                    exception.getMessage(),
                    source.sourceRef()
            );
            return Optional.empty();
        }
    }
}
