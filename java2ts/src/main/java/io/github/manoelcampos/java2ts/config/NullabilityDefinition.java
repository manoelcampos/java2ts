package io.github.manoelcampos.java2ts.config;

import java.util.List;

/**
 * Defines how nullable types (such as properties annotated with any of the
 * {@link Settings#nullableAnnotations()}) are represented in TypeScript.
 * Inline definitions write the union directly (such as {@code string | null}),
 * while the other ones use a {@code Nullable<T>} type alias (such as {@code Nullable<string>}).
 * @author Manoel Campos
 */
public enum NullabilityDefinition {
    /** {@code Nullable<string>}, where {@code type Nullable<T> = T | null | undefined}. */
    nullAndUndefinedUnion(false, "null", "undefined"),

    /** {@code Nullable<string>}, where {@code type Nullable<T> = T | null}. */
    nullUnion(false, "null"),

    /** {@code Nullable<string>}, where {@code type Nullable<T> = T | undefined}. */
    undefinedUnion(false, "undefined"),

    /** {@code string | null | undefined} */
    nullAndUndefinedInlineUnion(true, "null", "undefined"),

    /** {@code string | null} */
    nullInlineUnion(true, "null"),

    /** {@code string | undefined} */
    undefinedInlineUnion(true, "undefined");

    private final boolean inline;
    private final List<String> types;

    NullabilityDefinition(final boolean inline, final String... types) {
        this.inline = inline;
        this.types = List.of(types);
    }

    /**
     * {@return true if the union is written inline, false if the {@code Nullable<T>} alias is used}
     */
    public boolean isInline() {
        return inline;
    }

    /**
     * {@return the TypeScript types (null and/or undefined) added to a nullable type}
     */
    public List<String> types() {
        return types;
    }
}
