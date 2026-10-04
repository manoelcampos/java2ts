package io.github.manoelcampos.java2ts.validation.model;

import static java.util.Objects.requireNonNull;

/**
 * The schema given for a generic type parameter (such as {@code T}).
 * @param name the type parameter name
 * @author Manoel Campos
 */
public record TypeParameterSchema(String name) implements SchemaType {
    /**
     * Creates a {@link TypeParameterSchema}, validating the components.
     */
    public TypeParameterSchema {
        requireNonNull(name);
    }

    @Override
    public String describe() {
        return "type parameter " + name;
    }
}
