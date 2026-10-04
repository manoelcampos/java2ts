package io.github.manoelcampos.java2ts.validation.model;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A reference to the schema of a declared type.
 * @param typeName the TypeScript name of the referenced type
 * @param typeArguments the schemas of the generic type arguments
 * @author Manoel Campos
 */
public record ReferenceSchema(String typeName, List<SchemaType> typeArguments) implements SchemaType {
    /**
     * Creates a {@link ReferenceSchema}, validating the components and making immutable copies of collections.
     */
    public ReferenceSchema {
        requireNonNull(typeName);
        typeArguments = List.copyOf(typeArguments);
    }

    @Override
    public String describe() {
        return "object " + typeName;
    }
}
