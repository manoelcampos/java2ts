package io.github.manoelcampos.java2ts.ts;

import static java.util.Objects.requireNonNull;

/**
 * A TypeScript type written exactly as given, such as {@code string}, {@code number},
 * {@code null}, a string literal or any custom type defined by the user.
 * @param name the TypeScript code of the type
 * @author Manoel Campos
 */
public record TsBasicType(String name) implements TsType {
    /** The TypeScript {@code string} type. */
    public static final TsBasicType STRING = new TsBasicType("string");

    /** The TypeScript {@code number} type. */
    public static final TsBasicType NUMBER = new TsBasicType("number");

    /** The TypeScript {@code boolean} type. */
    public static final TsBasicType BOOLEAN = new TsBasicType("boolean");

    /** The TypeScript {@code void} type. */
    public static final TsBasicType VOID = new TsBasicType("void");

    /**
     * Creates a {@link TsBasicType}, validating the components and making immutable copies of collections.
     */
    public TsBasicType {
        requireNonNull(name);
    }

    /**
     * Creates a string literal type (such as {@code "RED"}), used for enum constants.
     * @param value the literal value
     * @return the string literal type
     */
    public static TsBasicType literal(final String value) {
        return new TsBasicType('"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"');
    }

    @Override
    public String format() {
        return name;
    }
}
