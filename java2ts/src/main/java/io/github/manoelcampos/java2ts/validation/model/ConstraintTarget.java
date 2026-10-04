package io.github.manoelcampos.java2ts.validation.model;

/**
 * The kinds of values a {@link Constraint} can be applied to.
 * @author Manoel Campos
 */
public enum ConstraintTarget {
    /** Strings. */
    TEXT,

    /** Numbers. */
    NUMBER,

    /** Booleans. */
    BOOLEAN,

    /** Dates that can be compared with the current date. */
    TEMPORAL,

    /** Arrays and collections. */
    ARRAY
}
