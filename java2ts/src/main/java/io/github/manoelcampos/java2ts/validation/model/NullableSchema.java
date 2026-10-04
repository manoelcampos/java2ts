package io.github.manoelcampos.java2ts.validation.model;

import static java.util.Objects.requireNonNull;

/**
 * A schema that also accepts null and/or undefined.
 * @param type the schema of the non-null values
 * @param acceptsNull if null is valid
 * @param acceptsUndefined if undefined is valid
 * @author Manoel Campos
 */
public record NullableSchema(SchemaType type, boolean acceptsNull, boolean acceptsUndefined) implements SchemaType {
    /**
     * Creates a {@link NullableSchema}, validating the components.
     */
    public NullableSchema {
        requireNonNull(type);
    }

    /**
     * {@return the schema of the non-null values}, or the given schema itself if it isn't nullable
     * @param type the schema to unwrap
     */
    public static SchemaType unwrap(final SchemaType type) {
        return type instanceof NullableSchema nullable ? nullable.type() : type;
    }

    /**
     * {@return a copy of this schema with another schema for the non-null values}
     * @param newType the new schema for the non-null values
     */
    public NullableSchema withType(final SchemaType newType) {
        return new NullableSchema(newType, acceptsNull, acceptsUndefined);
    }

    @Override
    public String describe() {
        return "nullable " + type.describe();
    }
}
