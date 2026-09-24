package dev.worldrpg.content.decode;

import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrictJsonStructureValidatorTest {
    @Test
    void duplicateRootKeyIsRejectedBeforeTreeDecode() {
        ValidationReport report = new ValidationReport();

        boolean valid = StrictJsonStructureValidator.validate(
                ContentSource.of(
                        "duplicate.json",
                        """
                        {
                          "schema": 1,
                          "registry": "world_rpg:registry/test",
                          "id": "world_rpg:test/a",
                          "id": "world_rpg:test/b"
                        }
                        """
                ),
                report
        );

        assertFalse(valid);
        assertTrue(
                report.messages().stream()
                        .anyMatch(message ->
                                message.code().equals("json.duplicate_key")
                        )
        );
    }

    @Test
    void duplicateNestedKeyIsRejected() {
        ValidationReport report = new ValidationReport();

        boolean valid = StrictJsonStructureValidator.validate(
                ContentSource.of(
                        "nested.json",
                        """
                        {
                          "schema": 1,
                          "registry": "world_rpg:registry/test",
                          "id": "world_rpg:test/a",
                          "effect": {
                            "amount": 5,
                            "amount": 6
                          }
                        }
                        """
                ),
                report
        );

        assertFalse(valid);
        assertTrue(
                report.messages().stream()
                        .anyMatch(message ->
                                message.message().contains("$.effect.amount")
                        )
        );
    }

    @Test
    void validNestedJsonPasses() {
        ValidationReport report = new ValidationReport();

        boolean valid = StrictJsonStructureValidator.validate(
                ContentSource.of(
                        "valid.json",
                        """
                        {
                          "schema": 1,
                          "registry": "world_rpg:registry/test",
                          "id": "world_rpg:test/a",
                          "array": [
                            {"x": 1},
                            {"x": 2}
                          ]
                        }
                        """
                ),
                report
        );

        assertTrue(valid);
        assertFalse(report.hasErrors());
    }
}
