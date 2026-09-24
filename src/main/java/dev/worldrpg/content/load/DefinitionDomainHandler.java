package dev.worldrpg.content.load;

import dev.worldrpg.api.data.DefinitionDomain;
import dev.worldrpg.api.data.RpgDefinition;
import dev.worldrpg.content.decode.DefinitionDecoder;

import java.util.Objects;

public record DefinitionDomainHandler<T extends RpgDefinition>(
        DefinitionDomain<T> domain,
        DefinitionDecoder<T> decoder,
        DefinitionResolver<T> resolver,
        DefinitionValidator<T> validator
) {
    public DefinitionDomainHandler {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(decoder, "decoder");
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(validator, "validator");
    }

    public static <T extends RpgDefinition> DefinitionDomainHandler<T> of(
            DefinitionDomain<T> domain,
            DefinitionDecoder<T> decoder
    ) {
        return new DefinitionDomainHandler<>(
                domain,
                decoder,
                DefinitionResolver.none(),
                DefinitionValidator.none()
        );
    }
}
