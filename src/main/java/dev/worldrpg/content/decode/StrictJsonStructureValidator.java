package dev.worldrpg.content.decode;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Strict structural JSON preflight.
 *
 * <p>Gson's object model cannot preserve evidence that an object contained the
 * same member name twice. This streaming pass rejects duplicate keys before
 * the authored document becomes a JsonObject.</p>
 */
public final class StrictJsonStructureValidator {
    private StrictJsonStructureValidator() {
    }

    public static boolean validate(
            ContentSource source,
            ValidationReport report
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(report, "report");

        boolean[] valid = {true};

        try (JsonReader reader =
                     new JsonReader(new StringReader(source.content()))) {
            reader.setLenient(false);

            readValue(
                    reader,
                    "$",
                    source,
                    report,
                    valid
            );

            if (reader.peek() != JsonToken.END_DOCUMENT) {
                report.error(
                        "json.trailing_content",
                        "JSON document contains content after the root value",
                        source.sourceRef()
                );
                valid[0] = false;
            }
        } catch (IOException | IllegalStateException exception) {
            report.error(
                    "json.syntax",
                    "Invalid strict JSON: " + exception.getMessage(),
                    source.sourceRef()
            );
            return false;
        }

        return valid[0];
    }

    private static void readValue(
            JsonReader reader,
            String path,
            ContentSource source,
            ValidationReport report,
            boolean[] valid
    ) throws IOException {
        JsonToken token = reader.peek();

        switch (token) {
            case BEGIN_OBJECT ->
                    readObject(reader, path, source, report, valid);
            case BEGIN_ARRAY ->
                    readArray(reader, path, source, report, valid);
            case STRING, NUMBER -> reader.nextString();
            case BOOLEAN -> reader.nextBoolean();
            case NULL -> reader.nextNull();
            default -> throw new IllegalStateException(
                    "Unexpected JSON token " + token + " at " + path
            );
        }
    }

    private static void readObject(
            JsonReader reader,
            String path,
            ContentSource source,
            ValidationReport report,
            boolean[] valid
    ) throws IOException {
        reader.beginObject();

        Set<String> names = new HashSet<>();

        while (reader.hasNext()) {
            String name = reader.nextName();
            String memberPath = path + "." + name;

            if (!names.add(name)) {
                report.error(
                        "json.duplicate_key",
                        "Duplicate JSON object key '"
                                + name
                                + "' at "
                                + memberPath,
                        source.sourceRef()
                );
                valid[0] = false;
            }

            readValue(
                    reader,
                    memberPath,
                    source,
                    report,
                    valid
            );
        }

        reader.endObject();
    }

    private static void readArray(
            JsonReader reader,
            String path,
            ContentSource source,
            ValidationReport report,
            boolean[] valid
    ) throws IOException {
        reader.beginArray();

        int index = 0;
        while (reader.hasNext()) {
            readValue(
                    reader,
                    path + "[" + index + "]",
                    source,
                    report,
                    valid
            );
            index++;
        }

        reader.endArray();
    }
}
