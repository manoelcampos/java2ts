package io.github.manoelcampos.java2ts.parser.type;

/**
 * Kinds of {@link java.util.Map} keys. Since JSON object keys are always strings,
 * keys that aren't numbers or enums are written as strings.
 * @author Manoel Campos
 */
public enum MapKeyKind {
    /** Any key that isn't a number or enum. */
    STRING,

    /** Number keys. */
    NUMBER,

    /** Enum keys, which may not include all enum constants. */
    ENUM
}
