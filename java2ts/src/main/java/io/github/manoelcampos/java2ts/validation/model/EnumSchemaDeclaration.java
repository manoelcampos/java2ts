package io.github.manoelcampos.java2ts.validation.model;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The schema of an enum, which accepts only the enum constant names.
 * @param typeName the name of the TypeScript type
 * @param constants the names of the enum constants, as they're serialized to JSON
 * @author Manoel Campos
 */
public record EnumSchemaDeclaration(String typeName, List<String> constants) implements SchemaDeclaration {
    /**
     * Creates an {@link EnumSchemaDeclaration}, validating the components and making immutable copies of collections.
     */
    public EnumSchemaDeclaration {
        requireNonNull(typeName);
        constants = List.copyOf(constants);
    }
}
