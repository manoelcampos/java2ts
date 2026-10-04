package io.github.manoelcampos.java2ts.validation.model;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The schema of a TypeScript interface, including all its properties (even the inherited ones).
 * @param typeName the name of the TypeScript interface
 * @param typeParameters the generic type parameters of the interface, including their bounds (such as {@code T extends number})
 * @param properties the properties of the interface, including the inherited ones
 * @author Manoel Campos
 */
public record ObjectSchemaDeclaration(String typeName, List<TypeParameter> typeParameters, List<SchemaProperty> properties)
    implements SchemaDeclaration
{
    /**
     * Creates an {@link ObjectSchemaDeclaration}, validating the components and making immutable copies of collections.
     */
    public ObjectSchemaDeclaration {
        requireNonNull(typeName);
        typeParameters = List.copyOf(typeParameters);
        properties = List.copyOf(properties);
    }

    /**
     * A generic type parameter.
     * @param name the type parameter name (such as {@code T})
     * @param declaration the TypeScript declaration of the type parameter, including its bounds (such as {@code T extends number})
     */
    public record TypeParameter(String name, String declaration) {}
}
