package io.github.manoelcampos.java2ts.validation.model;

import static java.util.Objects.requireNonNull;

/**
 * A property of an object schema.
 * Its optionality and nullability always match the TypeScript property with the same name.
 *
 * @param name the property name
 * @param type the schema of the property values (excluding null and undefined)
 * @param questionMark if the property may be absent (declared with a question mark in TypeScript)
 * @param acceptsNull if the property accepts null
 * @param acceptsUndefined if the property accepts undefined, even when it isn't declared with a question mark
 * @author Manoel Campos
 */
public record SchemaProperty(String name, SchemaType type, boolean questionMark, boolean acceptsNull, boolean acceptsUndefined) {
    /**
     * Creates a {@link SchemaProperty}, validating the components.
     */
    public SchemaProperty {
        requireNonNull(name);
        requireNonNull(type);
    }
}
