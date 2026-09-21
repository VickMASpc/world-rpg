package dev.worldrpg.content.decode;

import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.api.validation.ValidationReport;

import java.util.Optional;

@FunctionalInterface
public interface DefinitionDecoder<T extends RpgDefinition> {
    Optional<T> decode(DecodedJsonDocument document, ValidationReport report);
}
