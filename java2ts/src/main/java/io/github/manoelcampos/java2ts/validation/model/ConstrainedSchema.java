package io.github.manoelcampos.java2ts.validation.model;

import java.util.List;

/**
 * A {@link SchemaType} that accepts {@link Constraint}s.
 * @author Manoel Campos
 */
public sealed interface ConstrainedSchema extends SchemaType permits ScalarSchema, DateSchema, ArraySchema {
    /**
     * {@return the constraints of this schema}
     */
    List<Constraint> constraints();

    /**
     * {@return a copy of this schema including one more constraint}
     * @param constraint the constraint to add
     */
    ConstrainedSchema with(Constraint constraint);

    /**
     * {@return a new list with the given constraints plus another one}
     * @param constraints the existing constraints
     * @param constraint the constraint to add
     */
    static List<Constraint> append(final List<Constraint> constraints, final Constraint constraint) {
        final var list = new java.util.ArrayList<>(constraints);
        list.add(constraint);
        return list;
    }
}
