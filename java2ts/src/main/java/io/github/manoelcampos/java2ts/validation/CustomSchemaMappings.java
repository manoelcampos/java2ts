package io.github.manoelcampos.java2ts.validation;

import io.github.manoelcampos.java2ts.parser.type.CustomTypeMappings;
import io.github.manoelcampos.java2ts.parser.type.TypeMapper;
import io.github.manoelcampos.java2ts.parser.type.TypeMappingRule;
import io.github.manoelcampos.java2ts.parser.type.TypeNames;
import io.github.manoelcampos.java2ts.validation.model.CustomSchema;
import io.github.manoelcampos.java2ts.validation.model.SchemaType;
import io.github.manoelcampos.java2ts.validation.model.UnsupportedValidationException;

import java.lang.reflect.AnnotatedType;
import java.util.Map;
import java.util.Optional;

/**
 * Converts Java types to the schema expressions given by the user.
 * A type with a custom TypeScript mapping must have a custom schema mapping too,
 * since its TypeScript type is unknown to java2ts.
 * @author Manoel Campos
 */
final class CustomSchemaMappings implements TypeMappingRule<SchemaType> {
    private final CustomTypeMappings<?> tsMappings;
    private final CustomTypeMappings<?> schemaMappings;

    /**
     * Creates the custom schema mappings.
     * @param tsMappings the custom TypeScript type mappings
     * @param schemaMappings the custom schema mappings
     */
    CustomSchemaMappings(final Map<String, String> tsMappings, final Map<String, String> schemaMappings) {
        this.tsMappings = new CustomTypeMappings<>(tsMappings);
        this.schemaMappings = new CustomTypeMappings<>(schemaMappings);
    }

    @Override
    public Optional<SchemaType> map(final AnnotatedType type, final TypeMapper<SchemaType> mapper) {
        final Optional<SchemaType> schema = schemaMappings.find(type.getType()).map(CustomSchema::new);
        if (schema.isEmpty() && tsMappings.find(type.getType()).isPresent()) {
            final var msg = "type %s has a custom TypeScript mapping, so it requires a custom validation mapping too";
            throw new UnsupportedValidationException(msg.formatted(TypeNames.of(type.getType())));
        }

        return schema;
    }
}
