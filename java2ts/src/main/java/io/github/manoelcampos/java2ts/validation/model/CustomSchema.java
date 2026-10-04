package io.github.manoelcampos.java2ts.validation.model;

import static java.util.Objects.requireNonNull;

/**
 * A schema defined by the user through a custom type mapping.
 * @param expression the schema code, written as is into the generated file
 * @author Manoel Campos
 */
public record CustomSchema(String expression) implements SchemaType {
    /**
     * Creates a {@link CustomSchema}, validating the components.
     */
    public CustomSchema {
        requireNonNull(expression);
    }

    @Override
    public String describe() {
        return "custom schema " + expression;
    }
}
