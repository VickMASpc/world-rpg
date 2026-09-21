package dev.worldrpg.content.decode;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDocumentDecoderTest {
    @Test
    void decodesCommonHeader() {
        ValidationReport report = new ValidationReport();

        var result = JsonDocumentDecoder.decode(
                ContentSource.of(
                        "abilities/mage/frostbolt.json",
                        """
                        {
                          "schema": 1,
                          "id": "world_rpg:ability/mage/frostbolt",
                          "cast_time": 2.0
                        }
                        """
                ),
                report
        );

        assertTrue(result.isPresent());
        assertFalse(report.hasErrors());
        assertEquals(1, result.get().header().schema().value());
        assertEquals(
                RpgId.parse("world_rpg:ability/mage/frostbolt"),
                result.get().header().id()
        );
    }

    @Test
    void rejectsNonIntegerSchema() {
        ValidationReport report = new ValidationReport();

        var result = JsonDocumentDecoder.decode(
                ContentSource.of(
                        "bad.json",
                        """
                        {
                          "schema": 1.5,
                          "id": "world_rpg:test/bad"
                        }
                        """
                ),
                report
        );

        assertTrue(result.isEmpty());
        assertTrue(report.hasErrors());
    }

    @Test
    void rejectsInvalidJsonAndInvalidId() {
        ValidationReport syntaxReport = new ValidationReport();
        assertTrue(JsonDocumentDecoder.decode(
                ContentSource.of("syntax.json", "{ nope"),
                syntaxReport
        ).isEmpty());
        assertTrue(syntaxReport.hasErrors());

        ValidationReport idReport = new ValidationReport();
        assertTrue(JsonDocumentDecoder.decode(
                ContentSource.of(
                        "id.json",
                        """
                        {
                          "schema": 1,
                          "id": "NOT VALID"
                        }
                        """
                ),
                idReport
        ).isEmpty());
        assertTrue(idReport.hasErrors());
    }
}
