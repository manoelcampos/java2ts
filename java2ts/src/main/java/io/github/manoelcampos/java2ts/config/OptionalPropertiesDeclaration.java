package io.github.manoelcampos.java2ts.config;

import java.util.List;

/**
 * Defines how optional properties are declared in TypeScript.
 * A property is optional when {@link Settings#requiredAnnotations()} is not empty
 * and the property is not annotated with any of them.
 * @author Manoel Campos
 */
public enum OptionalPropertiesDeclaration {
    /** {@code name?: string} */
    questionMark(true),

    /** {@code name?: string | null} */
    questionMarkAndNullableType(true, "null"),

    /** {@code name: string | null} */
    nullableType(false, "null"),

    /** {@code name: string | null | undefined} */
    nullableAndUndefinableType(false, "null", "undefined"),

    /** {@code name: string | undefined} */
    undefinableType(false, "undefined");

    private final boolean usesQuestionMark;
    private final List<String> extraTypes;

    OptionalPropertiesDeclaration(final boolean usesQuestionMark, final String... extraTypes) {
        this.usesQuestionMark = usesQuestionMark;
        this.extraTypes = List.of(extraTypes);
    }

    /**
     * {@return true if optional properties are declared with a question mark after their names}
     */
    public boolean usesQuestionMark() {
        return usesQuestionMark;
    }

    /**
     * {@return the TypeScript types (null and/or undefined) added to the type of optional properties}
     */
    public List<String> extraTypes() {
        return extraTypes;
    }
}
